"use client";

import { forwardRef, useEffect, useImperativeHandle, useRef, useState } from "react";
import { ToolBox } from "@/components/tool-box";
import { ThoughtBox } from "@/components/thought-box";
import { Message, ToolInvocation } from "@/types/chat";

export interface ChatMessageHandle {
  addUserMessage: (content: string) => void;
  startAssistantMessage: () => void;
  appendAssistantText: (content: string) => void;
  setThinking: (content: string) => void;
  setStatus: (status: string) => void;
  addToolUse: (tool: ToolInvocation) => void;
  addToolResult: (callId: string, result: string) => void;
  addError: (message: string) => void;
  getMessages: () => Message[];
}

export const ChatMessage = forwardRef<ChatMessageHandle>(function ChatMessage(_props, ref) {
  const [messages, setMessages] = useState<Message[]>([]);
  const messagesRef = useRef<Message[]>([]);
  const scrollRef = useRef<HTMLDivElement>(null);

  const syncMessages = (updater: (prev: Message[]) => Message[]) => {
    const next = updater(messagesRef.current);
    messagesRef.current = next;
    setMessages(next);
  };

  useEffect(() => {
    scrollRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  useImperativeHandle(
    ref,
    () => ({
      addUserMessage: (content) => {
        syncMessages((prev) => [...prev, { role: "user", content }]);
      },
      startAssistantMessage: () => {
        syncMessages((prev) => [
          ...prev,
          { role: "assistant", content: "", tools: [] },
        ]);
      },
      appendAssistantText: (content) => {
        syncMessages((prev) => {
          if (prev.length === 0) return prev;
          const newMsgs = [...prev];
          const last = { ...newMsgs[newMsgs.length - 1] };
          last.content += content;
          newMsgs[newMsgs.length - 1] = last;
          return newMsgs;
        });
      },
      setThinking: (content) => {
        if (!content) return;
        syncMessages((prev) => {
          if (prev.length === 0) return prev;
          const newMsgs = [...prev];
          const last = { ...newMsgs[newMsgs.length - 1] };
          const existing = last.thinking ?? "";
          last.thinking = existing ? existing + content : content;
          newMsgs[newMsgs.length - 1] = last;
          return newMsgs;
        });
      },
      setStatus: (status) => {
        syncMessages((prev) => {
          if (prev.length === 0) return prev;
          const newMsgs = [...prev];
          const last = { ...newMsgs[newMsgs.length - 1] };
          last.status = status || undefined;
          newMsgs[newMsgs.length - 1] = last;
          return newMsgs;
        });
      },
      addToolUse: (tool) => {
        syncMessages((prev) => {
          if (prev.length === 0) return prev;
          const newMsgs = [...prev];
          const last = { ...newMsgs[newMsgs.length - 1] };
          last.tools = [...(last.tools || []), tool];
          newMsgs[newMsgs.length - 1] = last;
          return newMsgs;
        });
      },
      addToolResult: (callId, result) => {
        syncMessages((prev) => {
          if (prev.length === 0) return prev;
          const newMsgs = [...prev];
          const last = { ...newMsgs[newMsgs.length - 1] };
          last.tools = last.tools?.map((t) =>
            t.callId === callId ? { ...t, result, status: "done" } : t
          );
          newMsgs[newMsgs.length - 1] = last;
          return newMsgs;
        });
      },
      addError: (message) => {
        syncMessages((prev) => {
          if (prev.length === 0) return prev;
          const newMsgs = [...prev];
          const last = { ...newMsgs[newMsgs.length - 1] };
          last.content += `\n[Error: ${message}]`;
          newMsgs[newMsgs.length - 1] = last;
          return newMsgs;
        });
      },
      getMessages: () => messagesRef.current,
    }),
    []
  );

  return (
    <div>
      {messages.map((message, idx) => (
        <div key={`msg-${idx}`}>
          <div>{message.role}</div>
          {message.status && <div>{message.status}</div>}
          {message.content && <div>{message.content}</div>}
          {message.thinking?.trim() && <ThoughtBox content={message.thinking} />}
          {message.tools?.map((tool, tIdx) => (
            <ToolBox key={tool.callId || tIdx} tool={tool} />
          ))}
        </div>
      ))}
      <div ref={scrollRef} />
    </div>
  );
});
