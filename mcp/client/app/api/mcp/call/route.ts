import { callTool } from "@/src/mcp/manager";

export async function POST(req: Request) {
    try {
        const { server, toolId, input } = await req.json();
        const result = await callTool(server, toolId, input);
        return new Response(JSON.stringify({ result }), {
            status: 200,
            headers: { "Content-Type": "application/json" },
        });
    } catch (err: any) {
        return new Response(JSON.stringify({ error: err.message }), {
            status: 500,
            headers: { "Content-Type": "application/json" },
        });
    }
}
