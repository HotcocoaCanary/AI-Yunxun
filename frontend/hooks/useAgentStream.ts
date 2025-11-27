import { useEffect, useRef, useState } from "react";
import { AgentStreamEvent, sendQuestion } from "@/apis/agentApi";
import { MemoryScopePayload } from "@/types/chat";

export function useAgentStream() {
  const [isStreaming, setIsStreaming] = useState(false);
  const eventSourceRef = useRef<EventSource | null>(null);

  function startStreaming(
    question: string,
    memoryScope: MemoryScopePayload,
    onEvent: (event: AgentStreamEvent) => void
  ) {
    if (isStreaming) return;
    setIsStreaming(true);
    const es = sendQuestion({ question, memoryScope }, (event) => {
      if (event.type === "done" || event.type === "error") {
        setIsStreaming(false);
        eventSourceRef.current?.close();
        eventSourceRef.current = null;
      }
      onEvent(event);
    });
    eventSourceRef.current = es;
  }

  function stopStreaming() {
    eventSourceRef.current?.close();
    eventSourceRef.current = null;
    setIsStreaming(false);
  }

  useEffect(() => {
    return () => {
      eventSourceRef.current?.close();
    };
  }, []);

  return { isStreaming, startStreaming, stopStreaming };
}
