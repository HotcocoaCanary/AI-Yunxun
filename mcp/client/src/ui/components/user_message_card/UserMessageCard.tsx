"use client";

import type { Message } from "@/types/chat";

export function UserMessageCard({ message }: { message: Message }) {
  return (
    <div className="message message--user animate-fade-in">
      <div className="message-meta">👤 我</div>
      <div className="message-content">{message.content}</div>
    </div>
  );
}
