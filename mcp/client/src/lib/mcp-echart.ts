/**
 * ECharts MCP client wrapper using SSE transport.
 * Calls generate_graph_chart.
 */

import type { EchartGraphRequestBody, EchartGraphResponse } from "./types";

/**
 * Call ECharts MCP tool and return option or image payload.
 */
export async function callEchartMcpTool(
  baseUrl: string,
  toolName: "generate_graph_chart",
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
    const args: Record<string, unknown> = (() => {
      const nodes = (body.data?.nodes ?? []).map((node, index) => {
        const categoryName = node.category ?? "default";
        const name = node.id ?? node.name ?? `node-${index}`;
        return {
          name,
          categoryName,
          properties: {
            id: node.id,
            name: node.name,
            value: node.value,
          },
        };
      });

      const edges = (body.data?.edges ?? []).map((edge) => ({
        source: edge.source,
        target: edge.target,
        value: edge.value,
      }));

      const categorySet = new Set<string>();
      for (const node of body.data?.nodes ?? []) {
        categorySet.add(node.category ?? "default");
      }
      const categories = Array.from(categorySet).map((name) => ({
        name,
        symbol: "circle",
      }));

      return {
        title: body.title ?? undefined,
        layout: body.layout ?? "force",
        nodes,
        edges,
        categories,
      };
    })();

    const result = await client.callTool({
      name: toolName,
      arguments: args,
    });

    const content = (result as { content?: Array<{ type: string; text?: string; data?: string; mimeType?: string }> })
      .content;
    if (!content || content.length === 0) {
      throw new Error("MCP tool returned empty content.");
    }

    const first = content[0];
    if (first.type === "text") {
      const parsed = JSON.parse(first.text ?? "{}") as Record<string, unknown>;
      // Some MCP transports nest content inside first.text.
      let option: Record<string, unknown>;
      const inner = parsed?.content as Array<{ type?: string; text?: string }> | undefined;
      if (Array.isArray(inner) && inner[0]?.text != null) {
        option = (JSON.parse(inner[0].text) as Record<string, unknown>) ?? {};
      } else {
        option = parsed;
      }
      return { type: "option", option };
    }
    if (first.type === "image" && first.data != null && first.mimeType != null) {
      return { type: "image", data: first.data, mimeType: first.mimeType };
    }

    throw new Error(`Unsupported MCP response type: ${first.type}`);
  } finally {
    await transport.close();
  }
}