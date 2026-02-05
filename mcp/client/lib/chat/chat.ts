import {ZhipuAIClient, ZhipuChatResponse, ZhipuMessage} from "@/lib/llm/zhipu-ai";
import type { ZhipuChatRequest } from "@/lib/llm/zhipu-ai";
import {McpManager} from "@/lib/mcp/manager";

type ThinkingConfig = ZhipuChatRequest["thinking"];


export class McpChatService {
    constructor(
        private zhipu: ZhipuAIClient,
        private mcp: McpManager
    ) {}

    async chatRecursive(
        messages: ZhipuMessage[],
        onEvent: (type: string, data: any) => void,
        thinking?: ThinkingConfig
    ) {
        await this.mcp.init();
        const tools = await this.mcp.getToolsForLLM();
        let currentMessages = [...messages];
        let keepLooping = true;
        const thinkingEnabled = thinking?.type !== "disabled";

        while (keepLooping) {
            // 1. 询问智谱
            const res = await this.zhipu.chat({ messages: currentMessages, tools, stream: false, thinking });
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
                    const toolName = call.function.name;
                    const args = JSON.parse(call.function.arguments);

                    onEvent("tool_use", {
                        name: toolName,
                        args,
                        callId: call.id
                    });

                    const toolResult = await this.mcp.callTool(call.function.name, JSON.parse(call.function.arguments));

                    // --- 核心拦截逻辑 ---
                    let displayMetadata = { type: "text" };

                    // 逻辑：如果是 echart 前缀且返回的是合法的 JSON
                    if (toolName.startsWith("echart")) {
                        try {
                            JSON.parse(toolResult); // 验证是否为合法 JSON
                            displayMetadata.type = "echart";
                        } catch (e) {
                            console.warn("EChart 工具返回了非 JSON 格式");
                        }
                    }

                    onEvent("tool_result", {
                        name: call.function.name,
                        result: toolResult,
                        callId: call.id,
                        ui_type: displayMetadata.type
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
                let reasoningContent = "";
                let streamed = false;

                try {
                    const finalStream = await this.zhipu.chat({ messages: currentMessages, tools, stream: true, thinking });
                    const result = await this.pumpStream(finalStream, onEvent);
                    reasoningContent = result.reasoning;
                    streamed = true;
                } catch (e) {
                    console.warn("Streaming response failed, fallback to non-stream.", e);
                }

                if (!streamed && aiMsg?.content) {
                    onEvent("text", aiMsg.content);
                }

                if (thinkingEnabled) {
                    const finalReasoning = reasoningContent || aiMsg?.reasoning_content || "";
                    if (finalReasoning) {
                        onEvent("thinking", {
                            callId: "thinking",
                            name: "\u601D\u7EF4\u94FE",
                            content: finalReasoning,
                            ui_type: "text"
                        });
                    }
                }
                keepLooping = false;
            }
        }
    }

    private async pumpStream(
        response: Response,
        onEvent: (type: string, data: any) => void
    ): Promise<{ reasoning: string }> {
        const reader = response.body?.getReader();
        const decoder = new TextDecoder();
        if (!reader) return { reasoning: "" };

        let buffer = "";
        let reasoning = "";

        while (true) {
            const { done, value } = await reader.read();
            if (done) break;
            buffer += decoder.decode(value, { stream: true });
            let lineEnd = buffer.indexOf("\n");
            while (lineEnd >= 0) {
                let line = buffer.slice(0, lineEnd);
                buffer = buffer.slice(lineEnd + 1);
                if (line.endsWith("\r")) line = line.slice(0, -1);
                if (line.startsWith("data: ")) {
                    const dataStr = line.slice(6).trim();
                    if (dataStr === "[DONE]") break;
                    try {
                        const json = JSON.parse(dataStr);
                        const delta = json.choices?.[0]?.delta;
                        const content = delta?.content;
                        const reasoningDelta = delta?.reasoning_content;
                        if (content) onEvent("text", content);
                        if (reasoningDelta) reasoning += reasoningDelta;
                    } catch (e) {}
                }
                lineEnd = buffer.indexOf("\n");
            }
        }

        return { reasoning };
    }
}
