/**
 * 消息列表：渲染 user/assistant 消息，支持流式助手内容。
 * 数据来自 ChatPanel 的 messages 状态（SSE text 事件追加到当前助手消息）。
 */

"use client";

import React, { useEffect, useRef } from "react";

export interface MessageItem {
  id: string;
  role: "user" | "assistant";
  content: string;
}

export interface MessageListProps {
  messages: MessageItem[];
}

export default function MessageList({ messages }: MessageListProps) {
  const bottomRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  return (
    <div
      style={{
        padding: 16,
        display: "flex",
        flexDirection: "column",
        gap: 12,
        overflow: "auto",
        flex: 1,
      }}
    >
      {messages.length === 0 && (
        <div style={{ color: "#888", fontSize: 14 }}>
          发送消息开始对话；侧栏可展示工具状态与图表。
        </div>
      )}
      {messages.map((m) => (
        <div
          key={m.id}
          style={{
            alignSelf: m.role === "user" ? "flex-end" : "flex-start",
            maxWidth: "85%",
            padding: "10px 14px",
            borderRadius: 8,
            background: m.role === "user" ? "#e3f2fd" : "#f5f5f5",
            whiteSpace: "pre-wrap",
            wordBreak: "break-word",
          }}
        >
          <div style={{ fontSize: 12, color: "#666", marginBottom: 4 }}>
            {m.role === "user" ? "用户" : "助手"}
          </div>
          <div style={{ fontSize: 14 }}>{m.content || "…"}</div>
        </div>
      ))}
      <div ref={bottomRef} />
    </div>
  );
}
