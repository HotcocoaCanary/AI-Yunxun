import OpenAI from "openai";

export type ChatMessage = {
    role: "system" | "user" | "assistant";
    content: string;
};

type ChatCompletionInput = {
    messages?: ChatMessage[];
    model?: string;
};

export async function chatCompletion(input: ChatCompletionInput) {
    const apiKey = process.env.ZHIPUAI_API_KEY;
    if (!apiKey) {
        const err = new Error("Missing ZHIPUAI_API_KEY");
        (err as any).status = 500;
        throw err;
    }

    const messages = input.messages;
    if (!messages || !Array.isArray(messages) || messages.length === 0) {
        const err = new Error("messages is required");
        (err as any).status = 400;
        throw err;
    }

    const model = input.model ?? process.env.ZHIPUAI_MODEL ?? "glm-4.7-flash";

    const client = new OpenAI({
        apiKey,
        baseURL: "https://open.bigmodel.cn/api/paas/v4/",
    });

    const completion = await client.chat.completions.create({
        model,
        messages,
        stream: false,
    });

    const content = completion.choices?.[0]?.message?.content ?? "";

    return {
        id: completion.id,
        model: completion.model,
        content,
        usage: completion.usage,
    };
}
