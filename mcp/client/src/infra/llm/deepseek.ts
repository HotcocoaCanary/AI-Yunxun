import OpenAI from "openai";

export interface DeepSeekChatParams {
  messages: Array<Record<string, unknown>>;
  tools?: Array<Record<string, unknown>>;
  thinking?: boolean;
}

export class DeepSeekClient {
  private client: OpenAI;

  constructor() {
    this.client = new OpenAI({
      baseURL: process.env.BASE_URL!,
      apiKey: process.env.API_KEY!
    });
  }

  async chat(params: DeepSeekChatParams): Promise<Response> {
    const stream = await this.client.chat.completions.create({
      model: process.env.MODEL!,
      messages: params.messages,
      tools: params.tools,
      stream: true,
      ...(params.thinking
        ? { thinking: { type: "enabled" as const }, reasoning_effort: "high" as const }
        : {})
    } as unknown as OpenAI.Chat.Completions.ChatCompletionCreateParamsStreaming);

    const encoder = new TextEncoder();
    const readable = new ReadableStream({
      async start(controller) {
        for await (const chunk of stream) {
          const choice = chunk.choices?.[0];
          const delta: Record<string, unknown> = {};
          if (choice?.delta?.content !== undefined && choice?.delta?.content !== null) {
            delta.content = choice.delta.content;
          }
          if ((choice?.delta as Record<string, unknown>)?.reasoning_content !== undefined) {
            delta.reasoning_content = (choice?.delta as Record<string, unknown>).reasoning_content;
          }
          if (choice?.delta?.tool_calls !== undefined && choice?.delta?.tool_calls !== null) {
            delta.tool_calls = choice.delta.tool_calls;
          }
          const data: Record<string, unknown> = {
            choices: [
              {
                delta,
                finish_reason: choice?.finish_reason ?? null
              }
            ]
          };
          controller.enqueue(encoder.encode(`data: ${JSON.stringify(data)}\n\n`));
        }
        controller.enqueue(encoder.encode("data: [DONE]\n\n"));
        controller.close();
      }
    });

    return new Response(readable, {
      headers: { "Content-Type": "text/event-stream" }
    });
  }
}
