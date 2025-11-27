'use client';
import { useEffect, useMemo, useRef, useState } from "react";
import ChatSessionTreePanel from "@/components/chat/ChatSessionTreePanel";
import ChatConversationPanel from "@/components/chat/ChatConversationPanel";
import { useChatStore } from "@/store/chatStore";
import { ChatSessionApi } from "@/apis/chatSessionApi";
import { SettingApi } from "@/apis/settingApi";
import { computeMemoryScope } from "@/utils/chatMemory";
import { useAgentStream } from "@/hooks/useAgentStream";
import { ChatTreeNode } from "@/types/chat";
import { McpToolStatus } from "@/types/mcp";

function findNodeById(nodes: ChatTreeNode[], id?: string): ChatTreeNode | undefined {
  for (const node of nodes) {
    if (node.id === id) return node;
    if (node.children) {
      const found = findNodeById(node.children, id);
      if (found) return found;
    }
  }
  return undefined;
}

function findFirstSession(nodes: ChatTreeNode[]): string | undefined {
  for (const node of nodes) {
    if (node.type === "session") return node.id;
    if (node.children?.length) {
      const found = findFirstSession(node.children);
      if (found) return found;
    }
  }
  return undefined;
}

export default function ChatPage() {
  const [loadingTree, setLoadingTree] = useState(false);
  const [tools, setTools] = useState<McpToolStatus[]>([]);
  const streamingMessageIdRef = useRef<string | null>(null);
  const streamingSessionIdRef = useRef<string | null>(null);
  const {
    tree,
    activeNodeId,
    messagesBySession,
    setTree,
    setActiveNodeId,
    setMessagesForSession,
    appendMessage,
    updateMessage,
    createGroup,
    createSession,
    renameNode,
    deleteNode,
  } = useChatStore();
  const { isStreaming, startStreaming, stopStreaming } = useAgentStream();

  const activeMessages = activeNodeId ? messagesBySession[activeNodeId] || [] : [];
  const memoryScope = activeNodeId ? computeMemoryScope(tree, activeNodeId) : undefined;

  const loadTree = async () => {
    setLoadingTree(true);
    try {
      const data = await ChatSessionApi.getTree();
      setTree(data);
      if (!activeNodeId) {
        const first = findFirstSession(data) ?? data[0]?.id;
        if (first) setActiveNodeId(first);
      }
    } catch (error) {
      console.error(error);
    } finally {
      setLoadingTree(false);
    }
  };

  const loadMessages = async (sessionId: string) => {
    try {
      const list = await ChatSessionApi.getMessages(sessionId);
      setMessagesForSession(sessionId, list);
    } catch (error) {
      console.error(error);
      setMessagesForSession(sessionId, []);
    }
  };

  useEffect(() => {
    loadTree();
    SettingApi.getTools()
      .then((data) => setTools(data))
      .catch(() => setTools([]));
  }, []);

  useEffect(() => {
    if (activeNodeId && !messagesBySession[activeNodeId]) {
      loadMessages(activeNodeId);
    }
  }, [activeNodeId]);

  const handleSend = (text: string) => {
    const targetSession = activeNodeId ?? findFirstSession(tree) ?? `session-${Date.now()}`;
    if (!activeNodeId) setActiveNodeId(targetSession);
    const now = new Date().toISOString();
    appendMessage(targetSession, {
      id: `user-${Date.now()}`,
      role: "user",
      content: text,
      createdAt: now,
    });
    const scope = computeMemoryScope(tree, targetSession);
    streamingSessionIdRef.current = targetSession;
    streamingMessageIdRef.current = null;
    startStreaming(text, scope, (event) => {
      const sessionId = streamingSessionIdRef.current ?? targetSession;
      if (event.type === "answer_chunk") {
        const messageId =
          streamingMessageIdRef.current && event.content
            ? streamingMessageIdRef.current
            : `assistant-${Date.now()}`;
        streamingMessageIdRef.current = messageId;
        const existing = (useChatStore.getState().messagesBySession[sessionId] || []).find(
          (m) => m.id === messageId
        );
        if (!existing) {
          appendMessage(sessionId, {
            id: messageId,
            role: "assistant",
            content: event.content,
            createdAt: new Date().toISOString(),
          });
        } else {
          updateMessage(sessionId, messageId, (msg) => ({
            ...msg,
            content: `${msg.content}${event.content}`,
          }));
        }
      }
      if (event.type === "graph_update" && streamingMessageIdRef.current) {
        updateMessage(sessionId, streamingMessageIdRef.current, (msg) => ({
          ...msg,
          graph: event.graph,
        }));
      }
      if (event.type === "chart_update" && streamingMessageIdRef.current) {
        updateMessage(sessionId, streamingMessageIdRef.current, (msg) => ({
          ...msg,
          charts: event.charts,
        }));
      }
      if (event.type === "done" || event.type === "error") {
        streamingMessageIdRef.current = null;
        streamingSessionIdRef.current = null;
      }
    });
  };

  const handleStop = () => {
    streamingMessageIdRef.current = null;
    streamingSessionIdRef.current = null;
    stopStreaming();
  };

  const handleSelectNode = (nodeId: string) => {
    setActiveNodeId(nodeId);
    if (!messagesBySession[nodeId]) {
      loadMessages(nodeId);
    }
  };

  const activeNode = useMemo(() => findNodeById(tree, activeNodeId), [tree, activeNodeId]);

  return (
    <div className="chat-shell">
      <div className="chat-grid">
        <div className="chat-box">
          <ChatSessionTreePanel
            tree={tree}
            activeNodeId={activeNodeId}
            loading={loadingTree}
            onSelectNode={handleSelectNode}
            onCreateGroup={createGroup}
            onCreateSession={createSession}
            onRenameNode={renameNode}
            onDeleteNode={deleteNode}
          />
        </div>
        <div className="chat-box">
          <ChatConversationPanel
            messages={activeMessages}
            onSend={handleSend}
            onStop={handleStop}
            isStreaming={isStreaming}
          />
        </div>
      </div>
    </div>
  );
}
