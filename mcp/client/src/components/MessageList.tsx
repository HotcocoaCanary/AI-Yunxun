"use client";

import React, { useEffect, useRef } from "react";
import type { ChartPayload } from "@/lib/chat-stream";
import ChartMessage from "./ChartMessage";

export type MessageItem =
  | { id: string; role: "user" | "assistant"; type: "text"; content: string }
  | { id: string; role: "assistant"; type: "chart"; chart: ChartPayload };

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
        <div style={{ color: "#6b7280", fontSize: 14 }}>
          Send a message to start.
        </div>
      )}
      {messages.map((m) => {
        const isUser = m.role === "user";
        const bubbleStyle: React.CSSProperties = {
          alignSelf: isUser ? "flex-end" : "flex-start",
          maxWidth: "85%",
          padding: m.type === "chart" ? 10 : "10px 14px",
          borderRadius: 10,
          background: isUser ? "#e6f0ff" : "#f5f5f5",
          whiteSpace: "pre-wrap",
          wordBreak: "break-word",
          boxShadow: "0 1px 2px rgba(0,0,0,0.06)",
        };

        return (
          <div key={m.id} style={bubbleStyle}>
            <div style={{ fontSize: 12, color: "#6b7280", marginBottom: 6 }}>
              {isUser ? "You" : "Assistant"}
            </div>
            {m.type === "text" ? (
              <div style={{ fontSize: 14 }}>{m.content || "..."}</div>
            ) : (
              <ChartMessage chart={m.chart} />
            )}
          </div>
        );
      })}
      <div ref={bottomRef} />
    </div>
  );
}