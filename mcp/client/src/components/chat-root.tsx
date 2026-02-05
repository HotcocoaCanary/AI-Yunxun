"use client";

import { useRef } from "react";
import { ChatMessage, ChatMessageHandle } from "@/components/chat-message";
import { ChatInput } from "@/components/chat-input";

export default function ChatRoot() {
  const messageRef = useRef<ChatMessageHandle>(null);
  return (
    <div>
      <ChatMessage ref={messageRef} />
      <ChatInput messageRef={messageRef} />
    </div>
  );
}
