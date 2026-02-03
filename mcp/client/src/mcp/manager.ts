import { Client } from "@modelcontextprotocol/sdk/client/index.js";
import { SSEClientTransport } from "@modelcontextprotocol/sdk/client/sse.js";

let clientA: Client | null = null;
let clientB: Client | null = null;

export async function getMcpClientA() {
    if (!clientA) {
        const transport = new SSEClientTransport(
            new URL("http://localhost:8081/sse") // Server A SSE endpoint
        );
        clientA = new Client({ name: "demo-client-a", version: "1.0.0" });
        await clientA.connect(transport);
    }
    return clientA;
}

export async function getMcpClientB() {
    if (!clientB) {
        const transport = new SSEClientTransport(
            new URL("http://localhost:8082/sse") // Server B SSE endpoint
        );
        clientB = new Client({ name: "demo-client-b", version: "1.0.0" });
        await clientB.connect(transport);
    }
    return clientB;
}

// 聚合工具列表
export async function listAllTools() {
    const a = await getMcpClientA();
    const b = await getMcpClientB();

    const toolsA = await a.listTools();
    const toolsB = await b.listTools();

    return {
        a: "tools" in toolsA ? toolsA.tools : toolsA,
        b: "tools" in toolsB ? toolsB.tools : toolsB,
    };
}

// 调用工具
export async function callTool(server: "a" | "b", toolId: string, input: any) {
    const client = server === "a" ? await getMcpClientA() : await getMcpClientB();

    return await client.callTool({
        name: toolId,
        arguments: input
    });
}

