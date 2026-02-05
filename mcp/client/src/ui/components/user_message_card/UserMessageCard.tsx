"use client";

import type { Message } from "@/types/chat";

export function UserMessageCard({ message }: { message: Message }) {
  return (
    <div className="message message--user">
      <div className="message-meta">You</div>
      <div className="message-content">{message.content}</div>
    </div>
  );
}
