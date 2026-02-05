"use client";

import { useState } from "react";
import type { RefObject } from "react";
import { ChatMessageHandle } from "@/components/chat-message";

export function ChatInput({
  messageRef,
}: {
  messageRef: RefObject<ChatMessageHandle | null>;
}) {
  const [input, setInput] = useState("");
  const [isTyping, setIsTyping] = useState(false);
  const deepThinking = true;

  const handleSubmit = async () => {
    if (!input.trim() || isTyping) return;

    const text = input;
    setInput("");
    setIsTyping(true);
    let hadError = false;

    messageRef.current?.addUserMessage(text);
    messageRef.current?.startAssistantMessage();
    messageRef.current?.setStatus("thinking");

    try {
      const history = messageRef.current?.getMessages() ?? [];
      const response = await fetch("/api/chat", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ messages: history, deepThinking }),
      });

      if (!response.ok) throw new Error("Request failed");

      const reader = response.body?.getReader();
      const decoder = new TextDecoder();

      let buffer = "";
      let hasText = false;

      const handleSseEvent = (type: string, data: any) => {
        if (type === "text") {
          if (!hasText) {
            hasText = true;
            messageRef.current?.setStatus("responding");
          }
          messageRef.current?.appendAssistantText(data);
        } else if (type === "tool_use") {
          messageRef.current?.setStatus(`running tool: ${data.name}`);
          messageRef.current?.addToolUse({
            callId: data.callId,
            name: data.name,
            args: data.args,
            status: "running",
          });
        } else if (type === "tool_result") {
          messageRef.current?.setStatus(`tool done: ${data.name}`);
          messageRef.current?.addToolResult(data.callId, data.result);
        } else if (type === "thinking") {
          messageRef.current?.setStatus("reasoning");
          const payload = typeof data === "string" ? { content: data } : data ?? {};
          const content = payload.content ?? "";
          if (content) messageRef.current?.setThinking(content);
        } else if (type === "error") {
          hadError = true;
          messageRef.current?.setStatus("error");
          messageRef.current?.addError(data);
        }
      };

      while (true) {
        const { done, value } = await reader!.read();
        if (done) break;
        buffer += decoder.decode(value, { stream: true });
        const parts = buffer.split("\n\n");
        buffer = parts.pop() ?? "";

        parts.forEach((part) => {
          const line = part.trim();
          if (!line.startsWith("data: ")) return;
          try {
            const { type, data } = JSON.parse(line.replace("data: ", ""));
            handleSseEvent(type, data);
          } catch (e) {
            console.error("Failed to parse SSE packet:", e);
          }
        });
      }
      if (buffer.trim().startsWith("data: ")) {
        try {
          const { type, data } = JSON.parse(buffer.replace("data: ", "").trim());
          handleSseEvent(type, data);
        } catch (e) {
          console.error("Failed to parse SSE packet:", e);
        }
      }
      if (!hadError) {
        messageRef.current?.setStatus("done");
      }
    } catch (err: any) {
      console.error("Stream error:", err);
      hadError = true;
      messageRef.current?.setStatus("error");
      messageRef.current?.addError(err.message);
    } finally {
      setIsTyping(false);
    }
  };

  return (
    <div>
      <textarea
        rows={1}
        value={input}
        onChange={(e) => {
          setInput(e.target.value);
          e.target.style.height = "auto";
          e.target.style.height = e.target.scrollHeight + "px";
        }}
        onKeyDown={(e) => {
          if (e.key === "Enter" && !e.shiftKey) {
            e.preventDefault();
            handleSubmit();
          }
        }}
        disabled={isTyping}
      />
      <button onClick={handleSubmit} disabled={isTyping || !input.trim()}>
        Send
      </button>
    </div>
  );
}
