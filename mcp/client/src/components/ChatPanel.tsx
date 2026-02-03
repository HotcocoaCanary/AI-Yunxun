"use client";

import React, { useState, useCallback } from "react";
import MessageList, { type MessageItem } from "./MessageList";
import ChatInput from "./ChatInput";
import { streamChat, type ChatStreamEvent } from "@/lib/chat-stream";

export default function ChatPanel() {
  const [messages, setMessages] = useState<MessageItem[]>([]);
  const [loading, setLoading] = useState(false);

  const appendAssistantText = useCallback((text: string) => {
    setMessages((prev) => {
      const next = [...prev];
      const last = next[next.length - 1];
      if (last?.role === "assistant" && last.type === "text") {
        next[next.length - 1] = { ...last, content: last.content + text };
      } else {
        next.push({
          id: crypto.randomUUID(),
          role: "assistant",
          type: "text",
          content: text,
        });
      }
      return next;
    });
  }, []);

  const onSend = useCallback(
    async (message: string) => {
      setMessages((prev) => [
        ...prev,
        { id: crypto.randomUUID(), role: "user", type: "text", content: message },
        { id: crypto.randomUUID(), role: "assistant", type: "text", content: "" },
      ]);
      setLoading(true);

      try {
        await streamChat(message, (event: ChatStreamEvent) => {
          if (event.type === "text") {
            appendAssistantText(event.data);
          } else if (event.type === "chart") {
            setMessages((prev) => [
              ...prev,
              {
                id: crypto.randomUUID(),
                role: "assistant",
                type: "chart",
                chart: event.data,
              },
            ]);
          } else if (event.type === "tool_log") {
            console.debug(event.data);
          }
        });
      } catch (e) {
        const errMsg = e instanceof Error ? e.message : String(e);
        appendAssistantText(`\n[Error: ${errMsg}]`);
      } finally {
        setLoading(false);
      }
    },
    [appendAssistantText]
  );

  return (
    <div style={{ display: "flex", flexDirection: "column", height: "100%" }}>
      <MessageList messages={messages} />
      <ChatInput disabled={loading} onSend={onSend} />
    </div>
  );
}