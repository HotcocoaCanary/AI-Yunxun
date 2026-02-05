"use client";

import { ThoughtCard } from "@/ui/components/assistant_message_card/ThoughtCard";
import { ToolCard } from "@/ui/components/assistant_message_card/ToolCard";
import type { Message } from "@/types/chat";

export function AssistantMessageCard({ message }: { message: Message }) {
  return (
    <div className="message message--assistant">
      <div className="message-meta">
        <span>Assistant</span>
        {message.status && (
          <span className="message-status">{message.status}</span>
        )}
      </div>
      {message.content && (
        <div className="message-content">{message.content}</div>
      )}
      {message.thinking?.trim() && <ThoughtCard content={message.thinking} />}
      {message.tools?.map((tool, index) => (
        <ToolCard key={tool.callId || index} tool={tool} />
      ))}
    </div>
  );
}
