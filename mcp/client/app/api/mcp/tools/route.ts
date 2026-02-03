import { listAllTools } from "@/src/mcp/manager";

export async function GET() {
    const tools = await listAllTools();
    return new Response(JSON.stringify(tools), {
        status: 200,
        headers: { "Content-Type": "application/json" },
    });
}
