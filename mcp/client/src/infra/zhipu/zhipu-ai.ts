//infra/zhipu/zhipu-ai.ts
export interface ZhipuChatRequest {
    messages: any[];
    tools?: any[];
    web_search?: boolean;
    thinking?: boolean;
}

export class ZhipuAIClient {
    private apiKey = process.env.ZHIPUAI_API_KEY!;
    private url = "https://open.bigmodel.cn/api/paas/v4/chat/completions";

    async chat(
        params: ZhipuChatRequest
    ): Promise<Response> {

        const tools = params.web_search
            ? [
                ...(params.tools ?? []),
                {
                    type: "web_search",
                    web_search: {
                        search_engine: "search_std"
                    }
                }
            ]
            : params.tools;

        const response = await fetch(this.url, {
            method: "POST",
            headers: {
                Authorization: `Bearer ${this.apiKey}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                model: "glm-4.7-flash",
                messages: params.messages,
                tools,
                stream: true,
                temperature: 0.7,
                thinking: { type: params.thinking ? "enabled" : "disabled" }
            })
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(
                `智谱 API 错误 [${response.status}]: ${JSON.stringify(errorData)}`
            );
        }

        return response;
    }
}
