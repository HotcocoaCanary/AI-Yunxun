"use client";

import { useEffect, useRef, useState } from "react";
import { AssistantMessageCard } from "@/ui/components/assistant_message_card/AssistantMessageCard";
import { UserMessageCard } from "@/ui/components/user_message_card/UserMessageCard";
import { UserInputCard } from "@/ui/components/chat/UserInputCard";
import type { Message, ToolInvocation } from "@/types/chat";

export function ChatCard() {
  const [messages, setMessages] = useState<Message[]>([]);
  const messagesRef = useRef<Message[]>([]);
  const [isTyping, setIsTyping] = useState(false);
  const scrollRef = useRef<HTMLDivElement>(null);

  const syncMessages = (updater: (prev: Message[]) => Message[]) => {
    const next = updater(messagesRef.current);
    messagesRef.current = next;
    setMessages(next);
  };

  useEffect(() => {
    scrollRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  const addUserMessage = (content: string) => {
    syncMessages((prev) => [...prev, { role: "user", content }]);
  };

  const startAssistantMessage = () => {
    syncMessages((prev) => [...prev, { role: "assistant", content: "", tools: [] }]);
  };

  const appendAssistantText = (content: string) => {
    syncMessages((prev) => {
      if (prev.length === 0) return prev;
      const next = [...prev];
      const last = { ...next[next.length - 1] };
      last.content += content;
      next[next.length - 1] = last;
      return next;
    });
  };

  const setThinking = (content: string) => {
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
  };

  const setStatus = (status: string) => {
    syncMessages((prev) => {
      if (prev.length === 0) return prev;
      const next = [...prev];
      const last = { ...next[next.length - 1] };
      last.status = status || undefined;
      next[next.length - 1] = last;
      return next;
    });
  };

  const addToolUse = (tool: ToolInvocation) => {
    syncMessages((prev) => {
      if (prev.length === 0) return prev;
      const next = [...prev];
      const last = { ...next[next.length - 1] };
      last.tools = [...(last.tools || []), tool];
      next[next.length - 1] = last;
      return next;
    });
  };

  const addToolResult = (callId: string, result: string) => {
    syncMessages((prev) => {
      if (prev.length === 0) return prev;
      const next = [...prev];
      const last = { ...next[next.length - 1] };
      last.tools = last.tools?.map((tool) =>
        tool.callId === callId ? { ...tool, result, status: "done" } : tool
      );
      next[next.length - 1] = last;
      return next;
    });
  };

  const addError = (message: string) => {
    syncMessages((prev) => {
      if (prev.length === 0) return prev;
      const next = [...prev];
      const last = { ...next[next.length - 1] };
      last.content += `\n[Error: ${message}]`;
      next[next.length - 1] = last;
      return next;
    });
  };

  const handleSubmit = async (payload: {
    text: string;
    deepThinking: boolean;
    webSearch: boolean;
  }) => {
    if (!payload.text.trim() || isTyping) return;

    setIsTyping(true);
    let hadError = false;

    addUserMessage(payload.text);
    startAssistantMessage();
    setStatus("thinking");

    try {
      const history = messagesRef.current ?? [];
      const response = await fetch("/api/chat", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          messages: history,
          deepThinking: payload.deepThinking,
          webSearch: payload.webSearch
        })
      });

      if (!response.ok) throw new Error("Request failed");

      const reader = response.body?.getReader();
      const decoder = new TextDecoder();
      let buffer = "";
      let hasText = false;

      const handleSseEvent = (type: string, data: any) => {
        if (type === "text") {
          if (!hasText) {
            hasText = true;
            setStatus("responding");
          }
          appendAssistantText(data);
        } else if (type === "tool_use") {
          setStatus(`running tool: ${data.name}`);
          addToolUse({
            callId: data.callId,
            name: data.name,
            args: data.args,
            status: "running"
          });
        } else if (type === "tool_result") {
          setStatus(`tool done: ${data.name}`);
          addToolResult(data.callId, data.result);
        } else if (type === "thinking") {
          setStatus("reasoning");
          const payloadData =
            typeof data === "string" ? { content: data } : data ?? {};
          const content = payloadData.content ?? "";
          if (content) setThinking(content);
        } else if (type === "error") {
          hadError = true;
          setStatus("error");
          addError(data);
        }
      };

      while (true) {
        const { done, value } = await reader!.read();
        if (done) break;
        buffer += decoder.decode(value, { stream: true });
        const parts = buffer.split("\n\n");
        buffer = parts.pop() ?? "";

        parts.forEach((part) => {
          const line = part.trim();
          if (!line.startsWith("data: ")) return;
          try {
            const { type, data } = JSON.parse(line.replace("data: ", ""));
            handleSseEvent(type, data);
          } catch (e) {
            console.error("Failed to parse SSE packet:", e);
          }
        });
      }

      if (buffer.trim().startsWith("data: ")) {
        try {
          const { type, data } = JSON.parse(buffer.replace("data: ", "").trim());
          handleSseEvent(type, data);
        } catch (e) {
          console.error("Failed to parse SSE packet:", e);
        }
      }

      if (!hadError) {
        setStatus("done");
      }
    } catch (err: any) {
      console.error("Stream error:", err);
      hadError = true;
      setStatus("error");
      addError(err.message);
    } finally {
      setIsTyping(false);
    }
  };

  const isEmpty = messages.length === 0;

  return (
    <section className="chat-shell">
      <div className="chat-stream">
        {isEmpty ? (
          <div className="chat-empty">
            <div className="chat-empty-title">Start a new conversation</div>
            <div className="chat-empty-subtitle">
              Ask a question, run a tool, or explore ideas with MCP.
            </div>
          </div>
        ) : (
          messages.map((message, index) =>
            message.role === "user" ? (
              <UserMessageCard key={`msg-${index}`} message={message} />
            ) : (
              <AssistantMessageCard key={`msg-${index}`} message={message} />
            )
          )
        )}
        <div ref={scrollRef} />
      </div>
      <UserInputCard isTyping={isTyping} onSubmit={handleSubmit} />
    </section>
  );
}
