"use client";

import { ThoughtCard } from "@/ui/components/assistant_message_card/ThoughtCard";
import { ToolCard } from "@/ui/components/assistant_message_card/ToolCard";
import type { Message } from "@/types/chat";

export function AssistantMessageCard({ message }: { message: Message }) {
  return (
    <div>
      <div>assistant</div>
      {message.status && <div>{message.status}</div>}
      {message.content && (
        <div style={{ whiteSpace: "pre-wrap" }}>{message.content}</div>
      )}
      {message.thinking?.trim() && <ThoughtCard content={message.thinking} />}
      {message.tools?.map((tool, index) => (
        <ToolCard key={tool.callId || index} tool={tool} />
      ))}
    </div>
  );
}
