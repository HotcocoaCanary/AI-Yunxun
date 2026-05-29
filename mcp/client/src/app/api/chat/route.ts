import {DeepSeekClient} from "@/infra/llm/deepseek";
import {McpManager} from "@/infra/mcp/manager";
import {ServerChatService} from "@/domain/chat/ServerChatService";
import {LLMService} from "@/domain/llm/LLMService";

const deepseek = new DeepSeekClient();
const llm = new LLMService(deepseek);
const mcp = new McpManager({
    echart: process.env.ECHART_MCP_SERVER!,
    neo4j: process.env.NEO4J_MCP_SERVER!
});

export async function POST(req: Request) {
    const {messages, deepThinking} = await req.json() as Record<string, unknown>;
    const service = new ServerChatService(llm, mcp);
    const thinking = typeof deepThinking === "boolean" ? deepThinking : false;

    return new Response(new ReadableStream({
        async start(controller) {
            const encoder = new TextEncoder();
            const send = (type: string, data: unknown) => {
                controller.enqueue(encoder.encode(`data: ${JSON.stringify({ type, data })}\n\n`));
            };

            try {
                await service.chatRecursive(messages as Array<Record<string, unknown>>, send, {
                    thinking
                });
            } catch (err: unknown) {
                const message = err instanceof Error ? err.message : String(err);
                send("error", message);
            } finally {
                await mcp.cleanup();
                controller.close();
            }
        }
    }), {
        headers: {"Content-Type": "text/event-stream", "Cache-Control": "no-cache"}
    });
}
