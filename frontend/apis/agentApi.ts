import { MemoryScopePayload } from "@/types/chat";
import { ChartSpec } from "@/types/chart";
import { GraphData } from "@/types/graph";

export type AgentStreamEvent =
  | { type: "answer_chunk"; content: string }
  | { type: "graph_update"; graph: GraphData }
  | { type: "chart_update"; charts: ChartSpec[] }
  | { type: "done" }
  | { type: "error"; message: string };

export interface SendQuestionOptions {
  question: string;
  memoryScope: MemoryScopePayload;
}

export function sendQuestion(
  options: SendQuestionOptions,
  onEvent: (event: AgentStreamEvent) => void
): EventSource {
  const payload = encodeURIComponent(JSON.stringify(options));
  const url = `/api/chat/stream?payload=${payload}`;
  const es = new EventSource(url);

  es.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data) as AgentStreamEvent;
      onEvent(data);
    } catch (error) {
      onEvent({ type: "error", message: "解析流式数据失败" });
      console.error(error);
    }
  };

  es.onerror = () => {
    onEvent({ type: "error", message: "流式连接中断" });
    es.close();
  };

  return es;
}
