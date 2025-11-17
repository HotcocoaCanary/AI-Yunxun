'use client';

import { useCallback, useEffect, useMemo, useState } from 'react';
import { Loader2, Plus, Settings, User2 } from 'lucide-react';
import clsx from 'clsx';
import HistoryTree, { HistoryNode } from '@/components/HistoryTree';
import ToolTogglePanel, { ToolInfo } from '@/components/ToolTogglePanel';
import PlanTimeline, { PlanStep } from '@/components/PlanTimeline';
import ChartGallery from '@/components/ChartGallery';
import GraphRenderer from '@/components/GraphRenderer';
import ReactMarkdown from 'react-markdown';

interface AgentChartPayload {
  chartType: string;
  title: string;
  options: Record<string, any>;
}

interface AgentGraphPayload {
  nodes?: any[];
  links?: any[];
}

interface AgentDocumentSnippet {
  documentId: string;
  title: string;
  summary: string;
  source: string;
  url: string;
}

interface ConversationMessage {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: string;
  plan?: PlanStep[];
  charts?: AgentChartPayload[];
  graph?: AgentGraphPayload | null;
  documents?: AgentDocumentSnippet[];
}

interface ConversationSummary {
  id: string;
  title: string;
  updatedAt?: string;
}

interface AgentMessageDto {
  role: 'user' | 'assistant';
  content: string;
  timestamp?: string;
}

interface ConversationDetail {
  id: string;
  title: string;
  history: AgentMessageDto[];
  enabledTools: string[];
}

const DEFAULT_TOOLS = ['crawler', 'analysis', 'rag'];

export default function AgentWorkbench() {
  const [history, setHistory] = useState<HistoryNode[]>([]);
  const [selectedConversation, setSelectedConversation] = useState<string | undefined>();
  const [conversationId, setConversationId] = useState<string | undefined>();
  const [messages, setMessages] = useState<ConversationMessage[]>([]);
  const [availableTools, setAvailableTools] = useState<ToolInfo[]>([]);
  const [enabledTools, setEnabledTools] = useState<string[]>(DEFAULT_TOOLS);
  const [inputValue, setInputValue] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [crawlerSources, setCrawlerSources] = useState<string[]>(['arxiv.org', 'cnki.net']);
  const [newSource, setNewSource] = useState('');
  const [loadingConversations, setLoadingConversations] = useState(false);

  const formatUpdatedAt = (value?: string) => {
    if (!value) return undefined;
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return undefined;
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
  };

  const refreshConversations = useCallback(async () => {
    setLoadingConversations(true);
    try {
      const response = await fetch('/api/agent/conversations');
      if (!response.ok) {
        return;
      }
      const data: ConversationSummary[] = await response.json();
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
    } catch (error) {
      // ignore
    } finally {
      setLoadingConversations(false);
    }
  }, []);

  const handleSelectConversation = useCallback(async (id: string) => {
    setSelectedConversation(id);
    try {
      const response = await fetch(`/api/agent/conversations/${id}`);
      if (!response.ok) {
        return;
      }
      const detail: ConversationDetail = await response.json();
      setConversationId(detail.id);
      setEnabledTools(detail.enabledTools?.length ? detail.enabledTools : DEFAULT_TOOLS);
      const mappedHistory: ConversationMessage[] = (detail.history || []).map((msg, index) => ({
        id: `${msg.role}-${msg.timestamp || index}`,
        role: msg.role === 'assistant' ? 'assistant' : 'user',
        content: msg.content,
        timestamp: msg.timestamp ?? new Date().toISOString(),
      }));
      setMessages(mappedHistory);
    } catch (error) {
      // ignore
    }
  }, []);

  useEffect(() => {
    fetch('/api/agent/tools')
      .then((res) => res.json())
      .then((data) => setAvailableTools(data))
      .catch(() => {});
  }, []);

  useEffect(() => {
    refreshConversations();
  }, [refreshConversations]);

  const currentPlan = useMemo(() => {
    const lastAssistant = [...messages].reverse().find((msg) => msg.role === 'assistant' && msg.plan);
    return lastAssistant?.plan || [];
  }, [messages]);

  const currentCharts = useMemo(() => {
    const lastAssistant = [...messages].reverse().find((msg) => msg.role === 'assistant' && msg.charts);
    return lastAssistant?.charts || [];
  }, [messages]);

  const currentGraph = useMemo(() => {
    const lastAssistant = [...messages].reverse().find((msg) => msg.role === 'assistant' && msg.graph);
    return lastAssistant?.graph;
  }, [messages]);

  const currentDocs = useMemo(() => {
    const lastAssistant = [...messages].reverse().find((msg) => msg.role === 'assistant' && msg.documents);
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
    setCrawlerSources((prev) => Array.from(new Set([...prev, newSource.trim()])));
    setNewSource('');
  };

  const handleSend = async () => {
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
      const response = await fetch('/api/agent/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });
      const data = await response.json();
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
        content: '调用智能体失败，请检查后端服务或网络配置。',
        timestamp: new Date().toISOString(),
      };
      setMessages((prev) => [...prev, failure]);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="h-screen flex bg-gray-50">
      <aside className="w-80 border-r border-gray-200 flex flex-col bg-white">
        <div className="p-4 border-b border-gray-100">
          <div className="flex items-center justify-between mb-4">
            <div>
              <p className="text-sm font-semibold text-gray-900">论文智慧分析</p>
              <p className="text-xs text-gray-500">MCP Agent 控制台</p>
            </div>
            <button
              type="button"
              className="inline-flex items-center space-x-1 text-xs font-medium text-amber-600"
            >
              <Plus className="w-4 h-4" />
              <span>新对话</span>
            </button>
          </div>
          {loadingConversations ? (
            <div className="text-xs text-gray-400">加载会话...</div>
          ) : (
            <HistoryTree data={history} activeId={selectedConversation} onSelect={handleSelectConversation} />
          )}
        </div>
        <div className="p-4 space-y-4 overflow-y-auto flex-1">
          <ToolTogglePanel tools={availableTools} enabledTools={enabledTools} onToggle={toggleTool} />
          <div className="rounded-xl border border-gray-200 p-4 bg-white">
            <p className="text-sm font-semibold text-gray-900 mb-2">爬取站点</p>
            <div className="space-y-2 mb-3">
              {crawlerSources.map((source) => (
                <div key={source} className="flex items-center justify-between text-sm text-gray-600">
                  <span>{source}</span>
                </div>
              ))}
            </div>
            <div className="flex space-x-2">
              <input
                value={newSource}
                onChange={(e) => setNewSource(e.target.value)}
                placeholder="添加站点或 API"
                className="flex-1 border border-gray-200 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-amber-500"
              />
              <button
                type="button"
                onClick={handleAddSource}
                className="px-3 py-2 rounded-lg bg-amber-500 text-white text-sm"
              >
                添加
              </button>
            </div>
          </div>
        </div>
        <div className="p-4 border-t border-gray-100">
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 rounded-full bg-amber-100 flex items-center justify-center text-amber-600">
              <User2 className="w-5 h-5" />
            </div>
            <div className="flex-1">
              <p className="text-sm font-semibold text-gray-900">研究者工作台</p>
              <p className="text-xs text-gray-500">自定义 MCP 工具配置</p>
            </div>
            <button className="text-gray-400 hover:text-gray-600" type="button">
              <Settings className="w-5 h-5" />
            </button>
          </div>
        </div>
      </aside>

      <main className="flex-1 flex flex-col">
        <header className="border-b border-gray-200 p-4 bg-white flex items-center justify-between">
          <div>
            <p className="text-base font-semibold text-gray-900">智能体对话</p>
            <p className="text-xs text-gray-500">启用工具：{enabledTools.join(', ') || '无'}</p>
          </div>
          {isLoading && (
            <div className="flex items-center text-sm text-amber-600">
              <Loader2 className="w-4 h-4 mr-2 animate-spin" />
              智能体执行中...
            </div>
          )}
        </header>

        <div className="flex-1 overflow-y-auto p-6 space-y-6">
          <div className="space-y-4">
            {messages.map((message) => (
              <div
                key={message.id}
                className={message.role === 'user' ? 'ml-auto max-w-3xl' : 'mr-auto max-w-3xl'}
              >
                <div
                  className={clsx(
                    'rounded-2xl px-4 py-3 shadow-sm',
                    message.role === 'user' ? 'bg-amber-500 text-white' : 'bg-white border border-gray-200 text-gray-900'
                  )}
                >
                  <ReactMarkdown>{message.content}</ReactMarkdown>
                </div>
              </div>
            ))}
          </div>

          <PlanTimeline steps={currentPlan} />

          <ChartGallery charts={currentCharts} />

          <GraphRenderer graph={currentGraph} />

          {currentDocs.length > 0 && (
            <div className="card">
              <p className="text-sm font-semibold text-gray-900 mb-2">上下文文档</p>
              <div className="space-y-3">
                {currentDocs.map((doc) => (
                  <div key={doc.documentId} className="border border-gray-100 rounded-lg p-3">
                    <div className="flex items-center justify-between">
                      <p className="text-sm font-medium text-gray-900">{doc.title}</p>
                      <span className="text-xs text-gray-400">{doc.source}</span>
                    </div>
                    <p className="text-xs text-gray-500 overflow-hidden text-ellipsis whitespace-nowrap">
                      {doc.summary}
                    </p>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        <footer className="border-t border-gray-200 p-4 bg-white">
          <div className="flex space-x-3">
            <textarea
              value={inputValue}
              onChange={(e) => setInputValue(e.target.value)}
              placeholder="向智能体描述你的研究需求..."
              className="flex-1 resize-none border border-gray-200 rounded-xl px-4 py-3 focus:ring-2 focus:ring-amber-500"
              rows={2}
            />
            <button
              type="button"
              onClick={handleSend}
              disabled={isLoading || !inputValue.trim()}
              className="px-6 py-3 rounded-xl bg-amber-500 text-white font-semibold disabled:bg-gray-300 disabled:cursor-not-allowed"
            >
              发送
            </button>
          </div>
        </footer>
      </main>
    </div>
  );
}
