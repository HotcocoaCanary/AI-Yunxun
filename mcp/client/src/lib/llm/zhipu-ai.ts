// --- 基础子结构 ---
export interface ZhipuToolCall {
    id: string;
    type: "function";
    function: {
        name: string;
        arguments: string; // 注意：智谱返回的是 JSON 字符串
    };
}

export interface ZhipuMessage {
    role: "assistant" | "user" | "system" | "tool";
    content: string | null;
    tool_calls?: ZhipuToolCall[];
    tool_call_id?: string; // 仅 role 为 tool 时使用
    reasoning_content?: string; // 思维链内容
}

export interface ZhipuChatResponse {
    // 成功字段
    id?: string;
    request_id?: string;
    created?: number;
    model?: string;
    choices?: {
        index: number;
        finish_reason: "stop" | "tool_calls" | "length" | "sensitive" | "network_error";
        message: ZhipuMessage;
    }[];
    // 错误字段 (补全这里)
    error?: {
        code: string;
        message: string;
    };
    usage?: {
        prompt_tokens: number;
        completion_tokens: number;
        total_tokens: number;
    };
}

// --- 流式响应 Chunk (stream: true) ---
export interface ZhipuChatChunk {
    id: string;
    choices: {
        index: number;
        delta: {
            role?: "assistant";
            content?: string;
            reasoning_content?: string;
            tool_calls?: ZhipuToolCall[];
        };
        finish_reason: string | null;
    }[];
}

export interface ZhipuChatRequest {
    model: "glm-4.7-flash";
    messages: any[];
    tools?: any[];
    stream?: boolean;
    temperature?: number;
    tool_choice?: "auto";
    thinking?: {
        type: "enabled" | "disabled";
        clear_thinking?: boolean;
    };
}

export class ZhipuAIClient {
    private apiKey = process.env.ZHIPUAI_API_KEY;
    private url = 'https://open.bigmodel.cn/api/paas/v4/chat/completions';

    async chat(params: Omit<ZhipuChatRequest, 'model'>): Promise<Response> {
        const body: ZhipuChatRequest = {
            model: "glm-4.7-flash",
            messages: params.messages,
            tools: params.tools,
            stream: params.stream ?? false,
            temperature: params.temperature ?? 0.7,
            tool_choice: params.tool_choice ?? "auto",
            thinking: params.thinking ?? { type: "enabled", clear_thinking: true }
        };

        const response = await fetch(this.url, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${this.apiKey}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(body)
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(`智谱 API 错误 [${response.status}]: ${JSON.stringify(errorData)}`);
        }

        return response;
    }

    // 辅助方法：如果是非流式，可以快速解析出结果
    async parseJsonResponse(response: Response): Promise<ZhipuChatResponse> {
        return await response.json();
    }
}