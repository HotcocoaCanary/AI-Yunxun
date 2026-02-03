import { chatCompletion } from "@/src/chat/manager";

export async function POST(req: Request) {
    const body = await req.json().catch(() => null);
    const messages = body?.messages as
        | { role: "system" | "user" | "assistant"; content: string }[]
        | undefined;
    const model = body?.model;

    try {
        const result = await chatCompletion({ messages, model });
        return new Response(JSON.stringify(result), {
            status: 200,
            headers: { "Content-Type": "application/json" },
        });
    } catch (err: any) {
        const status = err?.status ?? 500;
        return new Response(JSON.stringify({ error: err?.message ?? "Unknown error" }), {
            status,
            headers: { "Content-Type": "application/json" },
        });
    }
}
