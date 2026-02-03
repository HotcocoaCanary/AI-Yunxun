export type ChartPayload =
  | { type: "option"; option: Record<string, unknown> }
  | { type: "image"; data: string; mimeType: string };

export type ChatStreamEvent =
  | { type: "status"; data: string }
  | { type: "text"; data: string }
  | { type: "chart"; data: ChartPayload }
  | { type: "tool_log"; data: string };

export async function streamChat(
  message: string,
  onEvent: (event: ChatStreamEvent) => void,
  signal?: AbortSignal
): Promise<void> {
  const res = await fetch("/api/chat", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ message }),
    signal,
  });

  if (!res.ok) {
    throw new Error(`Request failed: ${res.status}`);
  }

  const reader = res.body?.getReader();
  if (!reader) {
    throw new Error("No response body.");
  }

  const decoder = new TextDecoder();
  let buffer = "";

  while (true) {
    const { done, value } = await reader.read();
    if (done) break;
    buffer += decoder.decode(value, { stream: true });
    const parts = buffer.split("\n\n");
    buffer = parts.pop() ?? "";

    for (const block of parts) {
      let eventType = "";
      let data = "";
      for (const line of block.split("\n")) {
        if (line.startsWith("event: ")) eventType = line.slice(7).trim();
        if (line.startsWith("data: ")) data = line.slice(6);
      }

      if (eventType === "status") {
        onEvent({ type: "status", data });
      } else if (eventType === "text") {
        onEvent({ type: "text", data });
      } else if (eventType === "tool_log") {
        onEvent({ type: "tool_log", data });
      } else if (eventType === "chart") {
        try {
          const payload = JSON.parse(data) as ChartPayload;
          onEvent({ type: "chart", data: payload });
        } catch {
          onEvent({ type: "tool_log", data: "Failed to parse chart payload." });
        }
      }
    }
  }
}