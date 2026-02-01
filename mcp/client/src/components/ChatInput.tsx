/**
 * 对话输入：输入框 + 发送按钮。
 * 提交时 POST /api/chat，建立 SSE 连接，将 EventSource/ReadableStream 交给父组件或通过回调上报 text/status/chart。
 */

"use client";

import React, { useState, useCallback } from "react";

export interface ChatInputProps {
  disabled?: boolean;
  onSend: (message: string) => void;
}

export default function ChatInput({ disabled, onSend }: ChatInputProps) {
  const [value, setValue] = useState("");

  const handleSubmit = useCallback(
    (e: React.FormEvent) => {
      e.preventDefault();
      const msg = value.trim();
      if (!msg || disabled) return;
      onSend(msg);
      setValue("");
    },
    [value, disabled, onSend]
  );

  return (
    <form
      onSubmit={handleSubmit}
      style={{
        padding: 12,
        borderTop: "1px solid #e0e0e0",
        display: "flex",
        gap: 8,
        alignItems: "flex-end",
      }}
    >
      <textarea
        value={value}
        onChange={(e) => setValue(e.target.value)}
        placeholder="输入消息…"
        disabled={disabled}
        rows={2}
        style={{
          flex: 1,
          resize: "none",
          padding: 8,
          fontSize: 14,
          border: "1px solid #ccc",
          borderRadius: 6,
        }}
      />
      <button
        type="submit"
        disabled={disabled || !value.trim()}
        style={{
          padding: "8px 16px",
          fontSize: 14,
          border: "1px solid #333",
          borderRadius: 6,
          background: "#333",
          color: "#fff",
          cursor: disabled ? "not-allowed" : "pointer",
        }}
      >
        发送
      </button>
    </form>
  );
}
