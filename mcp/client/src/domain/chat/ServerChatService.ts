import { McpManager } from "@/infra/mcp/manager";
import { LLMService, type LLMToolCall } from "@/domain/llm/LLMService";

export class ServerChatService {
  constructor(
    private llm: LLMService,
    private mcp: McpManager
  ) {}

  async chatRecursive(
    messages: Array<Record<string, unknown>>,
    onEvent: (type: string, data: unknown) => void,
    options?: { thinking?: boolean }
  ) {
    await this.mcp.init();
    const tools = await this.mcp.getToolsForLLM();
    const currentMessages = [...messages];
    let keepLooping = true;

    while (keepLooping) {
      const result = await this.llm.chat({
        messages: currentMessages,
        tools,
        thinking: options?.thinking ?? false,
        onEvent
      });

      const shouldCallTools = result.toolCalls.length > 0
        && (result.finishReason === "tool_calls" || result.finishReason === null);

      if (shouldCallTools) {
        const assistantMsg: Record<string, unknown> = {
          role: "assistant",
          content: result.assistantContent || ""
        };
        if (result.reasoningContent) {
          assistantMsg.reasoning_content = result.reasoningContent;
        }
        if (result.toolCalls.length > 0) {
          assistantMsg.tool_calls = result.toolCalls;
        }
        currentMessages.push(assistantMsg);

        await this.handleToolCalls(result.toolCalls, currentMessages, onEvent);
      } else {
        keepLooping = false;
      }
    }
  }

  private async handleToolCalls(
    toolCalls: LLMToolCall[],
    currentMessages: Array<Record<string, unknown>>,
    onEvent: (type: string, data: unknown) => void
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
