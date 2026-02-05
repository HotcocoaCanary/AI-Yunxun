import {ZhipuAIClient, type ZhipuChatRequest} from "@/infra/zhipu/zhipu-ai";

export interface LLMToolCall {
    id: string;
    type: "function";
    function: {
        name: string;
        arguments: string;
    };
}

type ToolCallDelta = {
    id?: string;
    index?: number;
    type?: "function";
    function?: {
        name?: string;
        arguments?: string;
    };
};

type LLMChatResult = {
    assistantContent: string;
    toolCalls: LLMToolCall[];
    finishReason: string | null;
};

export class LLMService {
    constructor(private client: ZhipuAIClient) {
    }

    async chat(params: ZhipuChatRequest & {
        onEvent?: (type: string, data: any) => void;
    }): Promise<LLMChatResult> {
        const response = await this.client.chat({
            messages: params.messages,
            tools: params.tools,
            thinking: params.thinking,
            web_search: params.web_search
        });

        const reader = response.body?.getReader();
        if (!reader) {
            return await this.readNonStream(response, params.onEvent, params.thinking);
        }

        return await this.readStream(reader, params.onEvent, params.thinking);
    }

    private async readNonStream(
        response: Response,
        onEvent?: (type: string, data: any) => void,
        thinking?: boolean
    ): Promise<LLMChatResult> {
        const data = await response.json();
        const message = data?.choices?.[0]?.message;
        const finishReason = data?.choices?.[0]?.finish_reason ?? null;
        const content = message?.content ?? "";
        const reasoning = message?.reasoning_content ?? "";
        const toolCalls = (message?.tool_calls ?? []) as LLMToolCall[];

        if (content) onEvent?.("text", content);
        if (thinking && reasoning) {
            onEvent?.("thinking", {
                callId: "thinking",
                name: "\u601D\u7EF4\u94FE",
                content: reasoning,
                ui_type: "text"
            });
        }

        return {
            assistantContent: content,
            toolCalls,
            finishReason
        };
    }

    private async readStream(
        reader: ReadableStreamDefaultReader<Uint8Array>,
        onEvent?: (type: string, data: any) => void,
        thinking?: boolean
    ): Promise<LLMChatResult> {
        const decoder = new TextDecoder();
        let buffer = "";
        let assistantContent = "";
        let reasoningContent = "";
        let finishReason: string | null = null;
        const toolCalls: LLMToolCall[] = [];

        while (true) {
            const {done, value} = await reader.read();
            if (done) break;
            buffer += decoder.decode(value, {stream: true});
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
                        const choice = json?.choices?.[0];
                        const delta = choice?.delta;
                        if (choice?.finish_reason) finishReason = choice.finish_reason;

                        const content = delta?.content;
                        if (content) {
                            assistantContent += content;
                            onEvent?.("text", content);
                        }

                        const reasoningDelta = delta?.reasoning_content;
                        if (reasoningDelta) {
                            reasoningContent += reasoningDelta;
                            if (thinking) {
                                onEvent?.("thinking", {
                                    callId: "thinking",
                                    name: "\u601D\u7EF4\u94FE",
                                    content: reasoningDelta,
                                    ui_type: "text"
                                });
                            }
                        }

                        if (delta?.tool_calls) {
                            this.mergeToolCalls(toolCalls, delta.tool_calls as ToolCallDelta[]);
                        }
                    } catch {
                    }
                }
                lineEnd = buffer.indexOf("\n");
            }
        }

        return {
            assistantContent,
            toolCalls,
            finishReason
        };
    }

    private mergeToolCalls(target: LLMToolCall[], deltaCalls: ToolCallDelta[]) {
        for (let i = 0; i < deltaCalls.length; i += 1) {
            const delta = deltaCalls[i];
            const existingIndex = delta.id
                ? target.findIndex((call) => call.id === delta.id)
                : -1;
            const index = typeof delta.index === "number"
                ? delta.index
                : existingIndex >= 0
                    ? existingIndex
                    : target.length;
            const current = target[index] ?? {
                id: delta.id ?? `call_${index}`,
                type: "function" as const,
                function: {
                    name: "",
                    arguments: ""
                }
            };

            if (delta.id) current.id = delta.id;
            if (delta.type) current.type = delta.type;

            const fn = delta.function ?? {};
            if (fn.name) current.function.name = fn.name;
            if (typeof fn.arguments === "string") {
                current.function.arguments += fn.arguments;
            }

            target[index] = current;
        }
    }
}
