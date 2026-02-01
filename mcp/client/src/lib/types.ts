/**
 * 类型与常量：请求/响应、SSE 事件类型、环境变量名等。
 * 与 doc/echart-mcp-server-architecture.md 4.1、nextjs-client-architecture.md 一致。
 */

/** SSE 事件类型：status=状态，text=流式片段，chart=图表，tool_log=工具日志 */
export type SSEEventType = "status" | "text" | "chart" | "tool_log";

/** 图谱请求体：与 MCP 工具 generate_graph_chart / generate_graph_gl_chart 输入一致 */
export interface EchartGraphRequestBody {
  title?: string;
  data: {
    nodes: Array<{ id: string; name: string; value?: number; category?: string }>;
    edges?: Array<{ source: string; target: string; value?: number }>;
  };
  layout?: string;
  width?: number;
  height?: number;
  theme?: string;
  outputType?: "option" | "png" | "svg";
}

/** 图谱 API 响应：outputType=option 时 */
export interface EchartOptionResponse {
  type: "option";
  option: Record<string, unknown>;
}

/** 图谱 API 响应：outputType=png/svg 时 */
export interface EchartImageResponse {
  type: "image";
  data: string;
  mimeType: string;
}

export type EchartGraphResponse = EchartOptionResponse | EchartImageResponse;

/** 对话请求体 */
export interface ChatRequestBody {
  conversationId?: string;
  message: string;
}
