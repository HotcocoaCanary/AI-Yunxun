import { ZhipuAIClient } from "@/lib/llm/zhipu-ai";
import type { ZhipuChatRequest } from "@/lib/llm/zhipu-ai";
import { McpManager } from "@/lib/mcp/manager";
import {McpChatService} from "@/lib/chat/chat";

const zhipu = new ZhipuAIClient();
const mcp = new McpManager({
    echart: process.env.ECHART_MCP_SERVER!,
    neo4j: process.env.NEO4J_MCP_SERVER!
});

export async function POST(req: Request) {
    const { messages, deepThinking } = await req.json();
    const service = new McpChatService(zhipu, mcp);
    const thinking: ZhipuChatRequest["thinking"] | undefined =
        typeof deepThinking === "boolean"
            ? deepThinking
                ? { type: "enabled", clear_thinking: true }
                : { type: "disabled" }
            : undefined;

    return new Response(new ReadableStream({
        async start(controller) {
            const encoder = new TextEncoder();
            const send = (type: string, data: any) => {
                controller.enqueue(encoder.encode(`data: ${JSON.stringify({ type, data })}\n\n`));
            };

            try {
                await service.chatRecursive(messages, send, thinking);
            } catch (err: any) {
                send("error", err.message);
            } finally {
                await mcp.cleanup();
                controller.close();
            }
        }
    }), {
        headers: { 'Content-Type': 'text/event-stream', 'Cache-Control': 'no-cache' }
    });
}
