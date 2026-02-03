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
        borderTop: "1px solid #e5e7eb",
        display: "flex",
        gap: 8,
        alignItems: "flex-end",
        background: "rgba(255,255,255,0.7)",
        backdropFilter: "blur(8px)",
      }}
    >
      <textarea
        value={value}
        onChange={(e) => setValue(e.target.value)}
        placeholder="Type a message..."
        disabled={disabled}
        rows={2}
        style={{
          flex: 1,
          resize: "none",
          padding: 10,
          fontSize: 14,
          border: "1px solid #d1d5db",
          borderRadius: 10,
          background: "#fff",
        }}
      />
      <button
        type="submit"
        disabled={disabled || !value.trim()}
        style={{
          padding: "8px 16px",
          fontSize: 14,
          border: "1px solid #111827",
          borderRadius: 10,
          background: "#111827",
          color: "#fff",
          cursor: disabled ? "not-allowed" : "pointer",
        }}
      >
        Send
      </button>
    </form>
  );
}