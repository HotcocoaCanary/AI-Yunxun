/**
 * 主区：消息列表 + 输入框。
 * 维护 messages 状态；提交时 POST /api/chat，消费 SSE，按 event 类型更新消息、状态与图表回调。
 */

"use client";

import React, { useState, useCallback } from "react";
import MessageList from "./MessageList";
import ChatInput from "./ChatInput";

export interface ChatPanelProps {
  onStatus?: (status: string) => void;
  onChart?: (payload: { type: "option"; option: Record<string, unknown> } | { type: "image"; data: string; mimeType: string }) => void;
  onToolLog?: (log: string) => void;
}

export default function ChatPanel({ onStatus, onChart, onToolLog }: ChatPanelProps) {
  const [messages, setMessages] = useState<{ id: string; role: "user" | "assistant"; content: string }[]>([]);
  const [loading, setLoading] = useState(false);

  const onSend = useCallback(
    async (message: string) => {
      setMessages((prev) => [
        ...prev,
        { id: crypto.randomUUID(), role: "user", content: message },
        { id: crypto.randomUUID(), role: "assistant", content: "" },
      ]);
      setLoading(true);
      onStatus?.("思考中");

      try {
        const res = await fetch("/api/chat", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ message }),
        });
        if (!res.ok) {
          setMessages((prev) => {
            const next = [...prev];
            const last = next[next.length - 1];
            if (last?.role === "assistant") {
              next[next.length - 1] = { ...last, content: `[请求失败: ${res.status}]` };
            }
            return next;
          });
          onStatus?.("完成");
          return;
        }

        const reader = res.body?.getReader();
        if (!reader) {
          onStatus?.("完成");
          return;
        }

        const decoder = new TextDecoder();
        let buffer = "";
        let currentEvent = "";
        const appendAssistant = (text: string) => {
          setMessages((prev) => {
            const next = [...prev];
            const last = next[next.length - 1];
            if (last?.role === "assistant") {
              next[next.length - 1] = { ...last, content: last.content + text };
            }
            return next;
          });
        };

        while (true) {
          const { done, value } = await reader.read();
          if (done) break;
          buffer += decoder.decode(value, { stream: true });
          const parts = buffer.split("\n\n");
          buffer = parts.pop() ?? "";
          for (const block of parts) {
            let eventType = "";
            let data = "";
            for (const line of block.split("\n")) {
              if (line.startsWith("event: ")) eventType = line.slice(7).trim();
              if (line.startsWith("data: ")) data = line.slice(6);
            }
            if (eventType === "status") {
              onStatus?.(data);
            } else if (eventType === "text") {
              appendAssistant(data);
            } else if (eventType === "chart") {
              try {
                const payload = JSON.parse(data) as { type: "option"; option: Record<string, unknown> } | { type: "image"; data: string; mimeType: string };
                onChart?.(payload);
              } catch {
                onToolLog?.("chart 解析失败");
              }
            } else if (eventType === "tool_log") {
              onToolLog?.(data);
            }
          }
        }

        onStatus?.("完成");
      } catch (e) {
        const errMsg = e instanceof Error ? e.message : String(e);
        setMessages((prev) => {
          const next = [...prev];
          const last = next[next.length - 1];
          if (last?.role === "assistant") {
            next[next.length - 1] = { ...last, content: `[错误: ${errMsg}]` };
          }
          return next;
        });
        onStatus?.("完成");
      } finally {
        setLoading(false);
      }
    },
    [onStatus, onChart, onToolLog]
  );

  return (
    <div style={{ display: "flex", flexDirection: "column", height: "100%" }}>
      <MessageList messages={messages} />
      <ChatInput disabled={loading} onSend={onSend} />
    </div>
  );
}
