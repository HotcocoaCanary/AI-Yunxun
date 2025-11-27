'use client';

import {
  FormEvent,
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
} from 'react';

import '@/public/styles/agent.css';
import ConversationPanel from '@/components/agent/ConversationPanel';
import HeroSection from '@/components/agent/HeroSection';
import PersonalDrawer from '@/components/agent/PersonalDrawer';
import SidebarNav from '@/components/agent/SidebarNav';
import type {
  AgentChatResponse,
  AgentDocumentSnippet,
  ConversationDetail,
  ConversationMessage,
  ConversationSummary,
} from '@/components/agent/types';
import type { ToolInfo } from '@/components/ToolTogglePanel';
import type { HistoryNode } from '@/components/HistoryTree';
import {
  fetchConversations,
  fetchConversationDetail,
  fetchTools,
  postChat,
} from '@/lib/agent-api';

const DEFAULT_TOOLS = ['crawler', 'analysis', 'rag'];
const HERO_PROMPTS = [
  '今天有什么计划？',
  '我可以帮你做什么？',
  '开始新的研究对话？',
  '告诉我你的灵感，我来完善它。',
];

export default function AgentWorkbench() {
  const [history, setHistory] = useState<HistoryNode[]>([]);
  const [selectedConversation, setSelectedConversation] = useState<
    string | undefined
  >();
  const [conversationId, setConversationId] = useState<string | undefined>();
  const [messages, setMessages] = useState<ConversationMessage[]>([]);
  const [availableTools, setAvailableTools] = useState<ToolInfo[]>([]);
  const [enabledTools, setEnabledTools] = useState<string[]>(DEFAULT_TOOLS);
  const [inputValue, setInputValue] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [loadingConversations, setLoadingConversations] = useState(false);
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const [personalDrawerOpen, setPersonalDrawerOpen] = useState(false);
  const [crawlerSources, setCrawlerSources] = useState<string[]>([
    'arxiv.org',
    'cnki.net',
  ]);
  const [newSource, setNewSource] = useState('');
  const conversationEndRef = useRef<HTMLDivElement | null>(null);

  const heroTitle = useMemo(() => {
    const index = Math.floor(Math.random() * HERO_PROMPTS.length);
    return HERO_PROMPTS[index];
  }, []);

  const formatUpdatedAt = (value?: string) => {
    if (!value) return undefined;
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return undefined;
    return date.toLocaleTimeString('zh-CN', {
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const refreshConversations = useCallback(async () => {
    setLoadingConversations(true);
    try {
      const data = await fetchConversations<ConversationSummary[]>();
      const folder: HistoryNode = {
        id: 'folder-default',
        label: '历史对话',
        type: 'folder',
        children: (data || []).map((item) => ({
          id: item.id,
          label: item.title || '未命名对话',
          type: 'conversation',
          updatedAt: formatUpdatedAt(item.updatedAt),
        })),
      };
      setHistory([folder]);
    } finally {
      setLoadingConversations(false);
    }
  }, []);

  const handleSelectConversation = useCallback(async (id: string) => {
    setSelectedConversation(id);
    try {
      const detail = await fetchConversationDetail<ConversationDetail>(id);
      setConversationId(detail.id);
      setEnabledTools(
        detail.enabledTools?.length ? detail.enabledTools : DEFAULT_TOOLS,
      );
      const mappedHistory: ConversationMessage[] = (detail.history || []).map(
        (msg, index) => ({
          id: `${msg.role}-${msg.timestamp || index}`,
          role: msg.role === 'assistant' ? 'assistant' : 'user',
          content: msg.content,
          timestamp: msg.timestamp ?? new Date().toISOString(),
        }),
      );
      setMessages(mappedHistory);
    } catch {
      /* ignore */
    }
  }, []);

  useEffect(() => {
    fetchTools<ToolInfo[]>()
      .then((data) => setAvailableTools(data))
      .catch(() => {});
  }, []);

  useEffect(() => {
    refreshConversations();
  }, [refreshConversations]);

  useEffect(() => {
    if (conversationEndRef.current) {
      conversationEndRef.current.scrollIntoView({
        behavior: 'smooth',
        block: 'end',
      });
    }
  }, [messages, isLoading]);

  const currentPlan = useMemo(() => {
    const lastAssistant = [...messages]
      .reverse()
      .find((msg) => msg.role === 'assistant' && msg.plan);
    return lastAssistant?.plan || [];
  }, [messages]);

  const currentGraph = useMemo(() => {
    const lastAssistant = [...messages]
      .reverse()
      .find((msg) => msg.role === 'assistant' && msg.graph);
    return lastAssistant?.graph || null;
  }, [messages]);

  const currentDocs = useMemo<AgentDocumentSnippet[]>(() => {
    const lastAssistant = [...messages]
      .reverse()
      .find((msg) => msg.role === 'assistant' && msg.documents);
    return lastAssistant?.documents || [];
  }, [messages]);

  const toggleTool = (toolId: string, next: boolean) => {
    setEnabledTools((prev) => {
      if (next) {
        return Array.from(new Set([...prev, toolId]));
      }
      return prev.filter((id) => id !== toolId);
    });
  };

  const handleAddSource = () => {
    if (!newSource.trim()) return;
    setCrawlerSources((prev) =>
      Array.from(new Set([...prev, newSource.trim()])),
    );
    setNewSource('');
  };

  const handleSend = async (event?: FormEvent) => {
    event?.preventDefault();
    if (!inputValue.trim()) return;
    setIsLoading(true);
    const userMessage: ConversationMessage = {
      id: `user-${Date.now()}`,
      role: 'user',
      content: inputValue,
      timestamp: new Date().toISOString(),
    };
    setMessages((prev) => [...prev, userMessage]);
    const payload = {
      message: inputValue,
      conversationId,
      enabledTools,
    };
    setInputValue('');
    try {
      const data = await postChat<AgentChatResponse>(payload);
      const assistantMessage: ConversationMessage = {
        id: `assistant-${Date.now()}`,
        role: 'assistant',
        content: data.answer,
        timestamp: new Date().toISOString(),
        plan: data.plan,
        charts: data.charts,
        graph: data.graph,
        documents: data.documents,
      };
      setMessages((prev) => [...prev, assistantMessage]);
      setConversationId(data.conversationId);
      setSelectedConversation(data.conversationId);
      refreshConversations();
    } catch (error) {
      const failure: ConversationMessage = {
        id: `assistant-${Date.now()}`,
        role: 'assistant',
        content: '调用智能体失败，请稍后重试。',
        timestamp: new Date().toISOString(),
      };
      setMessages((prev) => [...prev, failure]);
      console.error(error);
    } finally {
      setIsLoading(false);
    }
  };

  const showEmptyState = messages.length === 0;

  return (
    <div className="workspace-shell">
      <SidebarNav
        sidebarOpen={sidebarOpen}
        loadingConversations={loadingConversations}
        history={history}
        selectedConversation={selectedConversation}
        onSelectConversation={handleSelectConversation}
        onToggleSidebar={() => setSidebarOpen((prev) => !prev)}
      />

      <div className="workspace-content">
        <header className="workspace-header">
          <div>
            <p className="workspace-header__label">智能体工作台</p>
            <p className="workspace-header__hint">实时掌握你的多模态研究进度</p>
          </div>
          <div className="workspace-header__actions">
            <button
              type="button"
              className="ghost-button"
              onClick={() => refreshConversations()}
            >
              刷新
            </button>
            <button
              type="button"
              className="primary-button"
              onClick={() => setPersonalDrawerOpen(true)}
            >
              个人中心
            </button>
          </div>
        </header>

        <main className="workspace-main">
          <HeroSection
            title={heroTitle}
            inputValue={inputValue}
            isLoading={isLoading}
            onInputChange={setInputValue}
            onSubmit={handleSend}
          />

          {showEmptyState ? (
            <section className="empty-state">
              <div className="empty-state__badge">欢迎</div>
              <h2>开始你的第一个研究任务</h2>
              <p>左侧选择历史会话，或在上方输入框描述新任务以启动对话。</p>
            </section>
          ) : (
            <ConversationPanel
              anchorRef={conversationEndRef}
              messages={messages}
              isLoading={isLoading}
              plan={currentPlan}
              graph={currentGraph}
              documents={currentDocs}
            />
          )}
        </main>
      </div>

      <PersonalDrawer
        open={personalDrawerOpen}
        onClose={() => setPersonalDrawerOpen(false)}
        availableTools={availableTools}
        enabledTools={enabledTools}
        onToggleTool={toggleTool}
        crawlerSources={crawlerSources}
        newSource={newSource}
        onNewSourceChange={setNewSource}
        onAddSource={handleAddSource}
      />
    </div>
  );
}
