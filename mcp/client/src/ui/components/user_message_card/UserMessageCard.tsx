"use client";

import type { Message } from "@/types/chat";

export function UserMessageCard({ message }: { message: Message }) {
  return (
    <div>
      <div>user</div>
      <div style={{ whiteSpace: "pre-wrap" }}>{message.content}</div>
    </div>
  );
}
