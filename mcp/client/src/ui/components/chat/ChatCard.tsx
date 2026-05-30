"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import { AssistantMessageCard } from "@/ui/components/assistant_message_card/AssistantMessageCard";
import { UserMessageCard } from "@/ui/components/user_message_card/UserMessageCard";
import { UserInputCard } from "@/ui/components/chat/UserInputCard";
import { mockSessions, type ChatSession } from "@/data/mockHistory";
import type { Message, ToolInvocation } from "@/types/chat";

const emptyHints = [
  "绘制一下邓超在演艺圈的人物关系",
  "我想了解一下海贼王世界的人物关系",
  "Java相关的技术栈，都是什么关系",
];

function fmtTime(dateStr: string): string {
  const now = new Date();
  const d = new Date(
    dateStr.replace(/-/g, "/") + ":00"
  );
  const diffMs = now.getTime() - d.getTime();
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));
  if (diffDays === 0) return "今天";
  if (diffDays === 1) return "昨天";
  if (diffDays < 7) return `${diffDays}天前`;
  if (diffDays < 30) return `${Math.floor(diffDays / 7)}周前`;
  return dateStr.slice(0, 10);
}

export function ChatCard() {
  const [sessions, setSessions] = useState<ChatSession[]>(() =>
    mockSessions.map((s) => ({
      ...s,
      messages: s.messages.map((m) => ({ ...m })),
    }))
  );
  const [activeSessionId, setActiveSessionId] = useState<string | null>(null);
  const [currentMessages, setCurrentMessages] = useState<Message[]>([]);
  const [isNewChat, setIsNewChat] = useState(true);
  const [isTyping, setIsTyping] = useState(false);
  const messagesRef = useRef<Message[]>([]);
  const scrollRef = useRef<HTMLDivElement>(null);

  const activeSession = sessions.find((s) => s.id === activeSessionId);

  useEffect(() => {
    messagesRef.current = currentMessages;
  }, [currentMessages]);

  useEffect(() => {
    scrollRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [currentMessages]);

  const syncMessages = useCallback(
    (updater: (prev: Message[]) => Message[]) => {
      setCurrentMessages((prev) => {
        const next = updater(prev);
        return next;
      });
    },
    []
  );

  const addUserMessage = useCallback(
    (content: string) => {
      syncMessages((prev) => [...prev, { role: "user", content }]);
    },
    [syncMessages]
  );

  const startAssistantMessage = useCallback(() => {
    syncMessages((prev) => [
      ...prev,
      { role: "assistant", content: "", tools: [] },
    ]);
  }, [syncMessages]);

  const appendAssistantText = useCallback(
    (content: string) => {
      syncMessages((prev) => {
        if (prev.length === 0) return prev;
        const next = [...prev];
        const last = { ...next[next.length - 1] };
        last.content += content;
        next[next.length - 1] = last;
        return next;
      });
    },
    [syncMessages]
  );

  const setThinking = useCallback(
    (content: string) => {
      if (!content) return;
      syncMessages((prev) => {
        if (prev.length === 0) return prev;
        const next = [...prev];
        const last = { ...next[next.length - 1] };
        const existing = last.thinking ?? "";
        last.thinking = existing ? existing + content : content;
        next[next.length - 1] = last;
        return next;
      });
    },
    [syncMessages]
  );

  const setStatus = useCallback(
    (status: string) => {
      syncMessages((prev) => {
        if (prev.length === 0) return prev;
        const next = [...prev];
        const last = { ...next[next.length - 1] };
        last.status = status || undefined;
        next[next.length - 1] = last;
        return next;
      });
    },
    [syncMessages]
  );

  const addToolUse = useCallback(
    (tool: ToolInvocation) => {
      syncMessages((prev) => {
        if (prev.length === 0) return prev;
        const next = [...prev];
        const last = { ...next[next.length - 1] };
        last.tools = [...(last.tools || []), tool];
        next[next.length - 1] = last;
        return next;
      });
    },
    [syncMessages]
  );

  const addToolResult = useCallback(
    (callId: string, result: string) => {
      syncMessages((prev) => {
        if (prev.length === 0) return prev;
        const next = [...prev];
        const last = { ...next[next.length - 1] };
        last.tools = last.tools?.map((tool) =>
          tool.callId === callId
            ? { ...tool, result, status: "done" as const }
            : tool
        );
        next[next.length - 1] = last;
        return next;
      });
    },
    [syncMessages]
  );

  const addError = useCallback(
    (message: string) => {
      syncMessages((prev) => {
        if (prev.length === 0) return prev;
        const next = [...prev];
        const last = { ...next[next.length - 1] };
        last.content += `\n[错误: ${message}]`;
        next[next.length - 1] = last;
        return next;
      });
    },
    [syncMessages]
  );

  const saveCurrentSession = useCallback(() => {
    if (currentMessages.length === 0) return;
    const title =
      currentMessages[0]?.content?.slice(0, 30) || "新对话";
    const now = new Date();
    const ts = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}-${String(now.getDate()).padStart(2, "0")} ${String(now.getHours()).padStart(2, "0")}:${String(now.getMinutes()).padStart(2, "0")}`;

    if (activeSessionId) {
      setSessions((prev) =>
        prev.map((s) =>
          s.id === activeSessionId
            ? { ...s, title, createdAt: ts, messages: [...currentMessages] }
            : s
        )
      );
    } else {
      const newSession: ChatSession = {
        id: `session-${Date.now()}`,
        title,
        createdAt: ts,
        messages: [...currentMessages],
      };
      setSessions((prev) => [newSession, ...prev]);
      setActiveSessionId(newSession.id);
    }
  }, [currentMessages, activeSessionId]);

  const handleSubmit = async (payload: {
    text: string;
    deepThinking: boolean;
  }) => {
    if (!payload.text.trim() || isTyping) return;

    if (isNewChat) {
      setIsNewChat(false);
    }

    setIsTyping(true);
    let hadError = false;

    addUserMessage(payload.text);
    startAssistantMessage();
    setStatus("思考中");

    try {
      const history = currentMessages.length > 0
        ? currentMessages
        : activeSession?.messages ?? [];
      const allMessages = [
        ...history,
        { role: "user", content: payload.text },
      ];

      const response = await fetch("/api/chat", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          messages: allMessages,
          deepThinking: payload.deepThinking,
        }),
      });

      if (!response.ok) throw new Error("请求失败");

      const reader = response.body?.getReader();
      const decoder = new TextDecoder();
      let buffer = "";
      let hasText = false;

      const handleSseEvent = (type: string, data: unknown) => {
        if (type === "text") {
          if (!hasText) {
            hasText = true;
            setStatus("回复中");
          }
          appendAssistantText(data as string);
        } else if (type === "tool_use") {
          const toolData = data as {
            callId: string;
            name: string;
            args: unknown;
          };
          setStatus(`工具调用: ${toolData.name}`);
          addToolUse({
            callId: toolData.callId,
            name: toolData.name,
            args: toolData.args,
            status: "running",
          });
        } else if (type === "tool_result") {
          const toolData = data as {
            callId: string;
            name: string;
            result: string;
          };
          setStatus(`工具完成: ${toolData.name}`);
          addToolResult(toolData.callId, toolData.result);
        } else if (type === "thinking") {
          setStatus("深度思考中");
          const payloadData =
            typeof data === "string"
              ? { content: data }
              : (data as Record<string, unknown> ?? {});
          const content = (payloadData.content ?? "") as string;
          if (content) setThinking(content);
        } else if (type === "error") {
          hadError = true;
          setStatus("错误");
          addError(data as string);
        }
      };

      while (reader) {
        const { done, value } = await reader.read();
        if (done) break;
        buffer += decoder.decode(value, { stream: true });
        const parts = buffer.split("\n\n");
        buffer = parts.pop() ?? "";

        parts.forEach((part) => {
          const line = part.trim();
          if (!line.startsWith("data: ")) return;
          try {
            const { type, data } = JSON.parse(
              line.replace("data: ", "")
            );
            handleSseEvent(type as string, data);
          } catch {
            // ignore parse errors
          }
        });
      }

      if (buffer.trim().startsWith("data: ")) {
        try {
          const { type, data } = JSON.parse(
            buffer.replace("data: ", "").trim()
          );
          handleSseEvent(type as string, data);
        } catch {
          // ignore parse errors
        }
      }

      if (!hadError) {
        setStatus("完成");
        setTimeout(() => {
          setStatus("");
          saveCurrentSession();
        }, 1000);
      }
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : String(err);
      hadError = true;
      setStatus("错误");
      addError(message);
    } finally {
      setIsTyping(false);
    }
  };

  const selectSession = useCallback(
    (session: ChatSession) => {
      if (isTyping && currentMessages.length > 0) {
        saveCurrentSession();
      }
      setCurrentMessages([...session.messages]);
      setActiveSessionId(session.id);
      setIsNewChat(false);
    },
    [isTyping, currentMessages, saveCurrentSession]
  );

  const deleteSession = useCallback(
    (e: React.MouseEvent, sessionId: string) => {
      e.stopPropagation();
      setSessions((prev) => prev.filter((s) => s.id !== sessionId));
      if (activeSessionId === sessionId) {
        setActiveSessionId(null);
        setCurrentMessages([]);
        setIsNewChat(true);
      }
    },
    [activeSessionId]
  );

  const newChat = useCallback(() => {
    if (isTyping && currentMessages.length > 0) {
      saveCurrentSession();
    }
    setCurrentMessages([]);
    setActiveSessionId(null);
    setIsNewChat(true);
  }, [isTyping, currentMessages, saveCurrentSession]);

  const handleSubmitRef = useRef(handleSubmit);
  handleSubmitRef.current = handleSubmit;

  const handleHintClick = useCallback(
    (hint: string) => {
      if (isTyping) return;
      handleSubmitRef.current({ text: hint, deepThinking: true });
    },
    [isTyping]
  );

  return (
    <div className="app-layout">
      <aside className="sidebar">
        <div className="sidebar-header">
          <div className="sidebar-logo">
            <div className="sidebar-logo-icon">AI</div>
            <div className="sidebar-logo-text">云寻助手</div>
          </div>
          <div className="sidebar-subtitle">
            智能运维 · MCP 驱动
          </div>
        </div>

        <div className="sidebar-actions">
          <button className="sidebar-new-chat-btn" onClick={newChat}>
            <svg
              width="16"
              height="16"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <line x1="12" y1="5" x2="12" y2="19" />
              <line x1="5" y1="12" x2="19" y2="12" />
            </svg>
            新建会话
          </button>
        </div>

        <div className="sidebar-sessions">
          <div className="sidebar-section-label">历史会话</div>
          {sessions.map((session) => (
            <button
              key={session.id}
              className={`session-item${activeSessionId === session.id ? " active" : ""}`}
              onClick={() => selectSession(session)}
            >
              <span className="session-icon">💬</span>
              <span className="session-info">
                <span className="session-title">{session.title}</span>
                <span className="session-time">
                  {fmtTime(session.createdAt)}
                </span>
              </span>
              <span
                className="session-delete"
                onClick={(e) => deleteSession(e, session.id)}
                title="删除会话"
              >
                ✕
              </span>
            </button>
          ))}
        </div>

        <div className="sidebar-footer">
          <div className="sidebar-status">
            <span className="sidebar-status-dot" />
            就绪
          </div>
          <span className="sidebar-version">v2.0</span>
        </div>
      </aside>

      <main className="main-content">
        <header className="main-header">
          <div className="main-header-title">
            {isNewChat ? "新会话" : activeSession?.title || "对话"}
          </div>
          {!isNewChat && (
            <div className="main-header-badge">
              {currentMessages.length > 0
                ? `${currentMessages.length} 条消息`
                : `历史会话`}
            </div>
          )}
          {isTyping && (
            <div className="main-header-badge" style={{ background: "#fef3c7", color: "#d97706" }}>
              <span style={{ display: "inline-flex", gap: 4 }}>
                <span className="typing-indicator">
                  <span />
                  <span />
                  <span />
                </span>
                回复中
              </span>
            </div>
          )}
        </header>

        <section className="chat-shell">
          <div className="chat-stream">
            {currentMessages.length === 0 ? (
              <div className="chat-empty animate-fade-in">
                <div className="chat-empty-icon">✨</div>
                <div className="chat-empty-title">
                  有什么我可以帮助你的？
                </div>
                <div className="chat-empty-subtitle">
                  选择一个历史会话或开始新的对话，我可以帮你分析数据、排查问题、编写代码等
                </div>
                <div className="chat-empty-hints">
                  {emptyHints.map((hint, i) => (
                    <button
                      key={i}
                      className="chat-empty-hint"
                      onClick={() => handleHintClick(hint)}
                      disabled={isTyping}
                    >
                      {hint}
                    </button>
                  ))}
                </div>
              </div>
            ) : (
              currentMessages.map((message, index) =>
                message.role === "user" ? (
                  <UserMessageCard
                    key={`msg-${index}`}
                    message={message}
                  />
                ) : (
                  <AssistantMessageCard
                    key={`msg-${index}`}
                    message={message}
                  />
                )
              )
            )}
            <div ref={scrollRef} />
          </div>
          <UserInputCard
            isTyping={isTyping}
            onSubmit={handleSubmit}
          />
        </section>
      </main>
    </div>
  );
}
