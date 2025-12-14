// 确保流式响应不被缓存
export const dynamic = 'force-dynamic';
export const runtime = 'nodejs';

const BACKEND_BASE_URL =
  process.env.BACKEND_BASE_URL || "http://localhost:8080";

type ChatRequestBody = {
  message: string;
  stream?: boolean;
};

type ToolCallInfo = {
  toolGroup: string;
  toolName: string;
  args: Record<string, unknown>;
};

type ChatResponse = {
  reply: string;
  graphJson?: string | null;
  chartJson?: string | null;
  toolCalls?: ToolCallInfo[];
};

type StreamEvent = {
  type: "tool_call" | "content" | "graph" | "chart" | "done";
  toolCall?: ToolCallInfo;
  content?: string;
  graphJson?: string;
  chartJson?: string;
};

export async function POST(req: Request) {
  const body = (await req.json()) as ChatRequestBody;

  // 如果请求流式输出
  if (body.stream) {
    console.log("Next.js API: 收到流式请求，消息:", body.message);
    const res = await fetch(`${BACKEND_BASE_URL}/api/chat/stream`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ message: body.message }),
    });

    console.log("Next.js API: 后端响应状态:", res.status, "Content-Type:", res.headers.get("Content-Type"));

    if (!res.ok) {
      console.error("Next.js API: 后端响应错误:", res.status, res.statusText);
      return new Response(
        JSON.stringify({ error: "Backend chat stream request failed" }),
        { status: 500 },
      );
    }

    // 直接转发后端响应流，不进行解析
    // 这样可以避免解析错误，让前端直接处理SSE格式
    console.log("Next.js API: 开始转发后端SSE流");
    
    if (!res.body) {
      console.error("Next.js API: 后端响应体为空");
      return new Response(
        JSON.stringify({ error: "Backend response body is empty" }),
        { status: 500 },
      );
    }

    // 直接返回后端响应流
    return new Response(res.body, {
      headers: {
        "Content-Type": "text/event-stream",
        "Cache-Control": "no-cache, no-transform",
        "X-Accel-Buffering": "no",
      },
    });

    return new Response(stream, {
      headers: {
        "Content-Type": "text/event-stream",
        "Cache-Control": "no-cache, no-transform",
        "X-Accel-Buffering": "no",
      },
    });
  }

  // 同步请求
  const res = await fetch(`${BACKEND_BASE_URL}/api/chat`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ message: body.message }),
  });

  if (!res.ok) {
    return new Response(
      JSON.stringify({ error: "Backend chat request failed" }),
      { status: 500 },
    );
  }

  const data = (await res.json()) as ChatResponse;

  return Response.json(data);
}

