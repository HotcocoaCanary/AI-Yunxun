import {ZhipuAIClient} from "@/infra/zhipu/zhipu-ai";
import {McpManager} from "@/infra/mcp/manager";
import {ServerChatService} from "@/domain/chat/ServerChatService";
import {LLMService} from "@/domain/llm/LLMService";

const zhipu = new ZhipuAIClient();
const llm = new LLMService(zhipu);
const mcp = new McpManager({
    echart: process.env.ECHART_MCP_SERVER!,
    neo4j: process.env.NEO4J_MCP_SERVER!
});

export async function POST(req: Request) {
    const {messages, deepThinking, webSearch} = await req.json();
    const service = new ServerChatService(llm, mcp);
    const thinking = typeof deepThinking === "boolean" ? deepThinking : false;
    const webSearchEnabled = typeof webSearch === "boolean" ? webSearch : false;

    return new Response(new ReadableStream({
        async start(controller) {
            const encoder = new TextEncoder();
            const send = (type: string, data: any) => {
                controller.enqueue(encoder.encode(`data: ${JSON.stringify({ type, data })}\n\n`));
            };

            try {
                await service.chatRecursive(messages, send, {
                    thinking,
                    webSearch: webSearchEnabled
                });
            } catch (err: any) {
                send("error", err.message);
            } finally {
                await mcp.cleanup();
                controller.close();
            }
        }
    }), {
        headers: {"Content-Type": "text/event-stream", "Cache-Control": "no-cache"}
    });
}
