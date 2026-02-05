import { McpManager } from "@/infra/mcp/manager";
import { LLMService, type LLMToolCall } from "@/domain/llm/LLMService";

export class ServerChatService {
  constructor(
    private llm: LLMService,
    private mcp: McpManager
  ) {}

  async chatRecursive(
    messages: any[],
    onEvent: (type: string, data: any) => void,
    options?: { thinking?: boolean; webSearch?: boolean }
  ) {
    await this.mcp.init();
    const tools = await this.mcp.getToolsForLLM();
    let currentMessages = [...messages];
    let keepLooping = true;

    while (keepLooping) {
      const result = await this.llm.chat({
        messages: currentMessages,
        tools,
        thinking: options?.thinking ?? false,
        web_search: options?.webSearch ?? false,
        onEvent
      });

      const shouldCallTools = result.toolCalls.length > 0
        && (result.finishReason === "tool_calls" || result.finishReason === null);

      if (shouldCallTools) {
        currentMessages.push({
          role: "assistant",
          content: result.assistantContent || "",
          tool_calls: result.toolCalls
        });

        await this.handleToolCalls(result.toolCalls, currentMessages, onEvent);
      } else {
        keepLooping = false;
      }
    }
  }

  private async handleToolCalls(
    toolCalls: LLMToolCall[],
    currentMessages: any[],
    onEvent: (type: string, data: any) => void
  ) {
    for (const call of toolCalls) {
      const args = this.safeParseArgs(call.function.arguments);

      onEvent("tool_use", {
        name: call.function.name,
        args,
        callId: call.id
      });

      const toolResult = await this.mcp.callTool(call.function.name, args);
      onEvent("tool_result", {
        name: call.function.name,
        result: toolResult,
        callId: call.id
      });

      currentMessages.push({
        role: "tool",
        content: toolResult,
        tool_call_id: call.id
      });
    }
  }

  private safeParseArgs(args: string) {
    try {
      return JSON.parse(args);
    } catch {
      return {};
    }
  }
}
