import { callEchartMcpTool } from "@/lib/mcp-echart";
import type { ChatRequestBody, EchartGraphRequestBody, EchartGraphResponse } from "@/lib/types";

const ENCODING = new TextEncoder();

function sseLine(event: string, data: string): string {
  return `event: ${event}\ndata: ${data}\n\n`;
}

type ToolCallState = {
  id?: string;
  name?: string;
  argumentsText: string;
};

type McpToolConfig = {
  type: "mcp";
  mcp: {
    server_label?: string;
    server_url?: string;
    transport_type?: "sse" | "streamable-http";
    allowed_tools?: string[];
    headers?: Record<string, string>;
  };
};

function buildMcpToolConfig(): McpToolConfig | null {
  const serverLabel = process.env.LLM_MCP_SERVER_LABEL;
  const serverUrl = process.env.ECHART_MCP_URL;
  const transport = process.env.LLM_MCP_TRANSPORT;
  const headersJson = process.env.LLM_MCP_HEADERS_JSON;

  if (!serverLabel && !serverUrl) {
    return null;
  }

  let headers: Record<string, string> | undefined;
  if (headersJson) {
    try {
      headers = JSON.parse(headersJson) as Record<string, string>;
    } catch {
      headers = undefined;
    }
  }

  return {
    type: "mcp",
    mcp: {
      server_label: serverLabel || undefined,
      server_url: serverLabel ? undefined : serverUrl,
      transport_type: transport === "sse" ? "sse" : "streamable-http",
      allowed_tools: ["generate_graph_chart", "get-neo4j-schema", "read-neo4j-cypher", "write-neo4j-cypher"],
      headers,
    },
  };
}

export async function POST(request: Request) {
  let body: ChatRequestBody;
  try {
    body = (await request.json()) as ChatRequestBody;
  } catch {
    return new Response(JSON.stringify({ error: "Request body must be JSON with message." }), {
      status: 400,
    });
  }

  const message = body?.message?.trim();
  if (!message) {
    return new Response(JSON.stringify({ error: "message is required" }), {
      status: 400,
    });
  }

  const stream = new ReadableStream<Uint8Array>({
    async start(controller) {
      let closed = false;\n      const safeClose = () => {\n        if (!closed) {\n          closed = true;\n          safeClose();\n        }\n      };\n\n      const push = (event: string, data: string) => {\n        if (closed) return;\n        controller.enqueue(ENCODING.encode(sseLine(event, data)));\n      };

      try {
        push("status", "thinking");

        const apiKey = process.env.LLM_API_KEY;
        const baseUrl = process.env.LLM_BASE_URL ?? "https://open.bigmodel.cn/api/paas/v4";

        if (!apiKey) {
          push("text", "[LLM_API_KEY is not configured.]");
          push("status", "done");
          safeClose();
          return;
        }

        const messages = [{ role: "user" as const, content: message }];

        const toolConfig = buildMcpToolConfig();
        const llmBody = {
          model: "glm-4-flash",
          messages,
          stream: true,
          temperature: 0.7,
          tools: toolConfig ? [toolConfig] : undefined,
        };

        const res = await fetch(`${baseUrl.replace(/\/$/, "")}/chat/completions`, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${apiKey}`,
          },
          body: JSON.stringify(llmBody),
        });

        if (!res.ok) {
          push("text", `[LLM request failed: ${res.status}]`);
          push("status", "done");
          safeClose();
          return;
        }

        const reader = res.body?.getReader();
        if (!reader) {
          push("text", "[No response body]");
          push("status", "done");
          safeClose();
          return;
        }

        const toolCalls = new Map<number, ToolCallState>();
        const decoder = new TextDecoder();
        let buffer = "";

        const recordToolCall = (toolCall: any) => {
          const index = typeof toolCall.index === "number" ? toolCall.index : 0;
          const existing = toolCalls.get(index) ?? { argumentsText: "" };
          const name = toolCall.function?.name ?? toolCall.mcp?.name ?? toolCall.name;
          const args = toolCall.function?.arguments ?? toolCall.mcp?.arguments ?? toolCall.arguments;

          if (toolCall.id) existing.id = toolCall.id;
          if (name) existing.name = name;
          if (typeof args === "string") {
            existing.argumentsText += args;
          }
          toolCalls.set(index, existing);
        };

        while (true) {
          const { done, value } = await reader.read();
          if (done) break;
          buffer += decoder.decode(value, { stream: true });
          const lines = buffer.split("\n");
          buffer = lines.pop() ?? "";
          for (const line of lines) {
            if (!line.startsWith("data: ") || line === "data: [DONE]") continue;
            const payload = line.slice(6);
            try {
              const json = JSON.parse(payload) as {
                choices?: Array<{
                  delta?: { content?: string; tool_calls?: Array<any> };
                  finish_reason?: string | null;
                }>;
              };
              const choice = json.choices?.[0];
              const delta = choice?.delta;
              if (typeof delta?.content === "string" && delta.content) {
                push("text", delta.content);
              }
              if (Array.isArray(delta?.tool_calls)) {
                for (const tc of delta.tool_calls) recordToolCall(tc);
              }
            } catch {
              // ignore parse error
            }
          }
        }

        if (toolCalls.size > 0) {
          for (const [, call] of Array.from(toolCalls.entries()).sort((a, b) => a[0] - b[0])) {
            if (!call.name) continue;
            let args: Record<string, unknown> = {};
            if (call.argumentsText) {
              try {
                args = JSON.parse(call.argumentsText) as Record<string, unknown>;
              } catch {
                push("tool_log", "Failed to parse tool arguments.");
              }
            }

            const chart = await invokeEchartToolAndGetChart(call.name, args);
            if (chart) {
              push("chart", JSON.stringify(chart));
            }
          }
        }

        push("status", "done");
      } catch (e) {
        const errMsg = e instanceof Error ? e.message : String(e);
        push("text", `[Error: ${errMsg}]`);
        push("status", "done");
      } finally {
        safeClose();
      }
    },
  });

  return new Response(stream, {
    headers: {
      "Content-Type": "text/event-stream",
      "Cache-Control": "no-cache",
      Connection: "keep-alive",
    },
  });
}

export async function invokeEchartToolAndGetChart(
  toolName: string,
  args: Record<string, unknown>
): Promise<EchartGraphResponse | null> {
  const url = process.env.ECHART_MCP_URL;
  if (!url) return null;

  const body: EchartGraphRequestBody = {
    title: args.title as string | undefined,
    data: args.data as EchartGraphRequestBody["data"],
    layout: (args.layout as string) ?? "force",
    width: (args.width as number) ?? 800,
    height: (args.height as number) ?? 600,
    theme: (args.theme as string) ?? "default",
    outputType: ((args.outputType as string) ?? "option") as "option" | "png" | "svg",
  };

  if (toolName === "generate_graph_chart") {
    return callEchartMcpTool(url, "generate_graph_chart", body);
  }
  return null;
}