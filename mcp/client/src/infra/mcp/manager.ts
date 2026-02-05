//infra/mcp/manager.ts
import { McpClient } from "./client";

export class McpManager {
    private clients: Map<string, McpClient> = new Map();

    constructor(private servers: Record<string, string>) {}

    async init() {
        for (const [label, url] of Object.entries(this.servers)) {
            if (this.clients.has(label)) continue;
            const client = new McpClient(label, url);
            await client.connect();
            this.clients.set(label, client);
        }
    }

    async getToolsForLLM() {
        const tools = [];
        for (const [label, client] of this.clients) {
            const result = await client.listTools();
            for (const t of result.tools) {
                tools.push({
                    type: "function",
                    function: {
                        name: `${label}__${t.name}`,
                        description: t.description,
                        parameters: t.inputSchema
                    }
                });
            }
        }
        return tools;
    }

    async callTool(prefixedName: string, args: any): Promise<string> {
        const [label, toolName] = prefixedName.split("__");
        const client = this.clients.get(label);

        if (!client) throw new Error(`MCP Client [${label}] not found`);

        const result = await client.callTool(toolName, args);

        // 类型安全提取：优先提取 text，否则转 JSON
        if (result.content && Array.isArray(result.content) && result.content.length > 0) {
            const first = result.content[0];
            if ('text' in first && typeof first.text === 'string') {
                return first.text;
            }
            return JSON.stringify(first);
        }

        return JSON.stringify(result);
    }

    async cleanup() {
        for (const client of this.clients.values()) {
            await client.close();
        }
        this.clients.clear();
    }
}