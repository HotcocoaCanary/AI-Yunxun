/**
 * 对话 API：POST /api/chat，返回 SSE 流。
 * 流程：调用 LLM 流式对话；若 LLM 返回 tool_calls，则通过 MCP 调用 Neo4j/ECharts，将结果喂回 LLM 或通过 SSE 推送 chart。
 * SSE 事件类型：status（思考中/完成）、text（流式片段）、chart（option 或 image）、可选 tool_log。
 * MCP 服务器地址从环境变量 ECHART_MCP_URL、NEO4J_MCP_URL 读取。
 */

import { callEchartMcpTool } from "@/lib/mcp-echart";
import type { ChatRequestBody, EchartGraphRequestBody, EchartGraphResponse } from "@/lib/types";

const ENCODING = new TextEncoder();

function sseLine(event: string, data: string): string {
  return `event: ${event}\ndata: ${data}\n\n`;
}

export async function POST(request: Request) {
  let body: ChatRequestBody;
  try {
    body = (await request.json()) as ChatRequestBody;
  } catch {
    return new Response(JSON.stringify({ error: "请求体须为 JSON，含 message" }), {
      status: 400,
    });
  }

  const message = body?.message?.trim();
  if (!message) {
    return new Response(JSON.stringify({ error: "message 不能为空" }), {
      status: 400,
    });
  }

  const stream = new ReadableStream<Uint8Array>({
    async start(controller) {
      const push = (event: string, data: string) => {
        controller.enqueue(ENCODING.encode(sseLine(event, data)));
      };

      try {
        push("status", "思考中");

        const apiKey = process.env.LLM_API_KEY;
        const baseUrl = process.env.LLM_BASE_URL ?? "https://open.bigmodel.cn/api/paas/v4";

        if (!apiKey) {
          push("text", "[未配置 LLM_API_KEY，仅演示 SSE。请配置后使用对话与 MCP 工具。]");
          push("status", "完成");
          controller.close();
          return;
        }

        const messages = [
          { role: "user" as const, content: message },
        ];

        const res = await fetch(`${baseUrl.replace(/\/$/, "")}/chat/completions`, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${apiKey}`,
          },
          body: JSON.stringify({
            model: "glm-4-flash",
            messages,
            stream: true,
            temperature: 0.7,
          }),
        });

        if (!res.ok) {
          push("text", `[LLM 请求失败: ${res.status}]`);
          push("status", "完成");
          controller.close();
          return;
        }

        const reader = res.body?.getReader();
        if (!reader) {
          push("text", "[无响应体]");
          push("status", "完成");
          controller.close();
          return;
        }

        const decoder = new TextDecoder();
        let buffer = "";
        while (true) {
          const { done, value } = await reader.read();
          if (done) break;
          buffer += decoder.decode(value, { stream: true });
          const lines = buffer.split("\n");
          buffer = lines.pop() ?? "";
          for (const line of lines) {
            if (line.startsWith("data: ") && line !== "data: [DONE]") {
              try {
                const json = JSON.parse(line.slice(6)) as { choices?: Array<{ delta?: { content?: string } }> };
                const content = json.choices?.[0]?.delta?.content;
                if (typeof content === "string" && content) {
                  push("text", content);
                }
              } catch {
                // ignore parse error
              }
            }
          }
        }

        push("status", "完成");
      } catch (e) {
        const errMsg = e instanceof Error ? e.message : String(e);
        push("text", `[错误: ${errMsg}]`);
        push("status", "完成");
      } finally {
        controller.close();
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

/**
 * 供内部或后续扩展：根据 tool_call 名称与参数调用 ECharts MCP 并返回 chart 结果。
 * 若为 generate_graph_chart / generate_graph_gl_chart，则返回 EchartGraphResponse，可推送给前端。
 */
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
  if (toolName === "generate_graph_gl_chart") {
    return callEchartMcpTool(url, "generate_graph_gl_chart", body);
  }
  return null;
}
