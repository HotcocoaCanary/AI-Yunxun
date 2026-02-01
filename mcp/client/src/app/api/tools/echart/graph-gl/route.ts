/**
 * 图谱直接调用接口：GL 关系图。
 * POST /api/tools/echart/graph-gl
 * 请求体与 doc 4.1 一致；通过 MCP SDK 调用 ECharts MCP 的 generate_graph_gl_chart。
 * 响应格式同 /api/tools/echart/graph。
 */

import { NextResponse } from "next/server";
import { callEchartMcpTool } from "@/lib/mcp-echart";
import type { EchartGraphRequestBody } from "@/lib/types";

export async function POST(request: Request) {
  const url = process.env.ECHART_MCP_URL;
  if (!url) {
    return NextResponse.json(
      { error: "ECHART_MCP_URL 未配置" },
      { status: 500 }
    );
  }

  let body: EchartGraphRequestBody;
  try {
    body = (await request.json()) as EchartGraphRequestBody;
  } catch {
    return NextResponse.json(
      { error: "请求体必须是 JSON，且包含 data.nodes" },
      { status: 400 }
    );
  }

  if (!body?.data?.nodes?.length) {
    return NextResponse.json(
      { error: "data.nodes 不能为空" },
      { status: 400 }
    );
  }

  try {
    const result = await callEchartMcpTool(url, "generate_graph_gl_chart", body);
    return NextResponse.json(result);
  } catch (e) {
    const message = e instanceof Error ? e.message : String(e);
    return NextResponse.json(
      { error: "调用 ECharts MCP 失败", detail: message },
      { status: 502 }
    );
  }
}
