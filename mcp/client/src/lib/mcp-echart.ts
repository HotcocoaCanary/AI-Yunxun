/**
 * ECharts MCP 客户端封装。
 * 通过 SSE 连接 Java ECharts MCP 服务，调用 generate_graph_chart / generate_graph_gl_chart。
 * 与 doc/echart-mcp-server-architecture.md、nextjs-client-architecture.md 一致。
 */

import type { EchartGraphRequestBody, EchartGraphResponse } from "./types";

const DEFAULT_OUTPUT_TYPE = "option";

/**
 * 调用 ECharts MCP 的 generate_graph_chart 或 generate_graph_gl_chart，返回 option 或 image。
 *
 * @param baseUrl ECharts MCP SSE 地址（如 http://localhost:8081/sse）
 * @param toolName "generate_graph_chart" 或 "generate_graph_gl_chart"
 * @param body 与 doc 4.1 一致的请求体
 * @returns { type: "option", option } 或 { type: "image", data, mimeType }
 */
export async function callEchartMcpTool(
  baseUrl: string,
  toolName: "generate_graph_chart" | "generate_graph_gl_chart",
  body: EchartGraphRequestBody
): Promise<EchartGraphResponse> {
  const [{ Client }, { SSEClientTransport }] = await Promise.all([
    import("@modelcontextprotocol/sdk/client"),
    import("@modelcontextprotocol/sdk/client/sse.js"),
  ]);
  const url = new URL(baseUrl);
  const transport = new SSEClientTransport(url);
  const client = new Client(
    { name: "client-next", version: "1.0.0" },
    { capabilities: {} }
  );

  await client.connect(transport);

  try {
    const args: Record<string, unknown> = {
      title: body.title ?? undefined,
      data: body.data,
      layout: body.layout ?? "force",
      width: body.width ?? 800,
      height: body.height ?? 600,
      theme: body.theme ?? "default",
      outputType: body.outputType ?? DEFAULT_OUTPUT_TYPE,
    };

    const result = await client.callTool({
      name: toolName,
      arguments: args,
    });

    const content = (result as { content?: Array<{ type: string; text?: string; data?: string; mimeType?: string }> })
      .content;
    if (!content || content.length === 0) {
      throw new Error("MCP 工具返回空 content");
    }

    const first = content[0];
    if (first.type === "text") {
      const option = JSON.parse(first.text ?? "{}") as Record<string, unknown>;
      return { type: "option", option };
    }
    if (first.type === "image" && first.data != null && first.mimeType != null) {
      return { type: "image", data: first.data, mimeType: first.mimeType };
    }

    throw new Error(`不支持的 MCP 返回类型: ${first.type}`);
  } finally {
    await transport.close();
  }
}
