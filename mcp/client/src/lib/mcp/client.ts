import { Client } from "@modelcontextprotocol/sdk/client/index.js";
import { SSEClientTransport } from "@modelcontextprotocol/sdk/client/sse.js";
import { CallToolResult, ListToolsResult } from "@modelcontextprotocol/sdk/types.js";

export class McpClient {
    private client: Client;
    private readonly transport: SSEClientTransport;

    constructor(private label: string, private url: string) {
        this.transport = new SSEClientTransport(new URL(`${this.url}`));
        this.client = new Client(
            { name: `client-${this.label}`, version: "1.0.0" },
            { capabilities: {} }
        );
    }

    async connect() {
        await this.client.connect(this.transport);
    }

    async listTools(): Promise<ListToolsResult> {
        return await this.client.listTools();
    }

    async callTool(name: string, args: any): Promise<CallToolResult> {
        return await this.client.callTool({
            name,
            arguments: args
        }) as CallToolResult;
    }

    async close() {
        await this.client.close();
    }
}