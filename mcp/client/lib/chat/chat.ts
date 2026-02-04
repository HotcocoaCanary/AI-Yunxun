import {ZhipuAIClient, ZhipuChatResponse, ZhipuMessage} from "@/lib/llm/zhipu-ai";
import {McpManager} from "@/lib/mcp/manager";


export class McpChatService {
    constructor(
        private zhipu: ZhipuAIClient,
        private mcp: McpManager
    ) {}

    async chatRecursive(
        messages: ZhipuMessage[],
        onEvent: (type: string, data: any) => void
    ) {
        await this.mcp.init();
        const tools = await this.mcp.getToolsForLLM();
        let currentMessages = [...messages];
        let keepLooping = true;

        while (keepLooping) {
            // 1. 询问智谱
            const res = await this.zhipu.chat({ messages: currentMessages, tools, stream: false });
            const data = (await res.json()) as ZhipuChatResponse;

            if (data.error) throw new Error(data.error.message);

            const aiMsg = data.choices?.[0]?.message;
            const finishReason = data.choices?.[0]?.finish_reason;

            if (finishReason === "tool_calls" && aiMsg?.tool_calls) {
                // 记录模型意图
                currentMessages.push({
                    role: "assistant",
                    content: aiMsg.content || "",
                    tool_calls: aiMsg.tool_calls
                });

                // 2. 执行工具
                for (const call of aiMsg.tool_calls) {
                    onEvent("tool_use", {
                        name: call.function.name,
                        args: JSON.parse(call.function.arguments),
                        callId: call.id
                    });

                    const toolResult = await this.mcp.callTool(call.function.name, JSON.parse(call.function.arguments));

                    onEvent("tool_result", {
                        name: call.function.name,
                        result: toolResult,
                        callId: call.id
                    });

                    // 回填工具结果
                    currentMessages.push({
                        role: "tool",
                        content: toolResult,
                        tool_call_id: call.id
                    });
                }
            } else {
                // 3. 任务收尾：流式输出最终回复
                if (!aiMsg?.content) {
                    const finalStream = await this.zhipu.chat({ messages: currentMessages, stream: true });
                    await this.pumpStream(finalStream, onEvent);
                } else {
                    onEvent("text", aiMsg.content);
                }
                keepLooping = false;
            }
        }
    }

    private async pumpStream(response: Response, onEvent: (type: string, data: any) => void) {
        const reader = response.body?.getReader();
        const decoder = new TextDecoder();
        if (!reader) return;

        while (true) {
            const { done, value } = await reader.read();
            if (done) break;
            const chunk = decoder.decode(value);
            const lines = chunk.split('\n');
            for (const line of lines) {
                if (line.startsWith('data: ')) {
                    const dataStr = line.slice(6).trim();
                    if (dataStr === '[DONE]') break;
                    try {
                        const json = JSON.parse(dataStr);
                        const content = json.choices[0].delta?.content;
                        if (content) onEvent("text", content);
                    } catch (e) {}
                }
            }
        }
    }
}