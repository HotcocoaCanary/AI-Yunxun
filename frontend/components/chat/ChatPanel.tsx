"use client";

import { useState } from "react";
import { ChartPanel, type ChartResponse } from "@/components/chart/ChartPanel";
import type { GraphData } from "@/components/graph/GraphPanel";

type ChatMessage = {
  id: string;
  role: "user" | "assistant";
  text: string;
  toolCalls?: ToolCallInfo[];
};

type ToolCallInfo = {
  toolGroup: string;
  toolName: string;
  args: Record<string, unknown>;
};

type StreamEvent = {
  type: "tool_call" | "content" | "graph" | "chart" | "done";
  toolCall?: ToolCallInfo;
  content?: string;
  graphJson?: string;
  chartJson?: string;
};

const BACKEND_ERROR_MESSAGE = "后端暂时不可用，请稍后重试。";

type ChatPanelProps = {
  onGraphChange?: (graph: GraphData | null) => void;
};

/**
 * Main chat + MCP interaction area.
 * 当前作为 MCP 客户端入口，调用 Next API /api/chat，再由后端接入 Ollama + MCP。
 */
export function ChatPanel({ onGraphChange }: ChatPanelProps) {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const [chart, setChart] = useState<ChartResponse | null>(null);
  const [currentToolCall, setCurrentToolCall] = useState<ToolCallInfo | null>(null);
  const [streamingMessageId, setStreamingMessageId] = useState<string | null>(null);

  async function handleSend() {
    const trimmed = input.trim();
    if (!trimmed || loading) return;

    const userMsg: ChatMessage = {
      id: crypto.randomUUID(),
      role: "user",
      text: trimmed,
    };
    setMessages((prev) => [...prev, userMsg]);
    setInput("");
    setLoading(true);
    setCurrentToolCall(null);

    // 创建助手消息占位符
    const assistantMsgId = crypto.randomUUID();
    setStreamingMessageId(assistantMsgId);
    const assistantMsg: ChatMessage = {
      id: assistantMsgId,
      role: "assistant",
      text: "",
      toolCalls: [],
    };
    setMessages((prev) => [...prev, assistantMsg]);

    try {
      // 使用流式请求
      const res = await fetch("/api/chat", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ message: trimmed, stream: true }),
      });

      if (!res.ok) {
        throw new Error("Chat API error");
      }

      const reader = res.body?.getReader();
      const decoder = new TextDecoder();
      let accumulatedText = "";
      const toolCalls: ToolCallInfo[] = [];

      if (!reader) {
        throw new Error("No reader available");
      }

      console.log("前端: 开始读取SSE流");
      let eventCount = 0;
      let buffer = "";
      let currentEventType = "";
      let pendingData = "";
      let hasReceivedData = false;
      
      // 处理SSE事件的函数
      const processEvent = (event: StreamEvent) => {
        eventCount++;
        console.log(`前端: 收到SSE事件 #${eventCount}:`, event.type, "内容:", event.content ? event.content.substring(0, 50) : "无内容");

        if (event.type === "tool_call" && event.toolCall) {
          setCurrentToolCall(event.toolCall);
          toolCalls.push(event.toolCall);
          setMessages((prev) =>
            prev.map((msg) =>
              msg.id === assistantMsgId
                ? { ...msg, toolCalls: [...toolCalls] }
                : msg,
            ),
          );
        } else if (event.type === "content") {
          // 即使content为空字符串也要更新
          if (event.content !== undefined) {
            accumulatedText += event.content;
            setMessages((prev) =>
              prev.map((msg) =>
                msg.id === assistantMsgId ? { ...msg, text: accumulatedText } : msg,
              ),
            );
            console.log("前端: 更新消息内容，当前长度:", accumulatedText.length);
          }
        } else if (event.type === "graph" && event.graphJson) {
          if (onGraphChange) {
            try {
              const parsedGraph = JSON.parse(event.graphJson) as GraphData;
              onGraphChange(parsedGraph);
              console.log("前端: 更新图谱数据");
            } catch (e) {
              console.error("前端: 解析图谱JSON失败:", e);
              onGraphChange(null);
            }
          }
        } else if (event.type === "chart" && event.chartJson) {
          try {
            const parsedChart = JSON.parse(event.chartJson) as ChartResponse;
            setChart(parsedChart);
            console.log("前端: 更新图表数据");
          } catch (e) {
            console.error("前端: 解析图表JSON失败:", e);
            setChart(null);
          }
        } else if (event.type === "done") {
          setCurrentToolCall(null);
          console.log("前端: 收到完成事件");
        }
      };
      
      while (true) {
        const { done, value } = await reader.read();
        
        if (done) {
          if (!hasReceivedData) {
            console.warn("前端: 流立即结束，没有收到任何数据");
          }
          // 处理剩余数据
          if (pendingData.trim()) {
            try {
              const event: StreamEvent = JSON.parse(pendingData.trim());
              processEvent(event);
            } catch (e) {
              console.error("前端: 处理剩余数据失败:", e, "数据:", pendingData.substring(0, 100));
            }
          }
          console.log(`前端: 流结束，共收到 ${eventCount} 个事件`);
          break;
        }

        if (!value || value.length === 0) {
          continue;
        }

        hasReceivedData = true;
        const chunk = decoder.decode(value, { stream: true });
        buffer += chunk;
        console.log("前端: 收到数据块，长度:", chunk.length, "内容预览:", chunk.substring(0, Math.min(150, chunk.length)));
        console.log("前端: 当前buffer长度:", buffer.length, "pendingData长度:", pendingData.length);
        console.log("前端: buffer完整内容:", JSON.stringify(buffer.substring(0, Math.min(200, buffer.length))));
        
        // 逐行处理SSE格式（支持 \n 和 \r\n）
        let processedAnyLine = false;
        while (true) {
          let newlineIndex = buffer.indexOf("\n");
          if (newlineIndex === -1) {
            newlineIndex = buffer.indexOf("\r\n");
          }
          if (newlineIndex === -1) {
            // 没有换行符了，等待更多数据
            break;
          }
          
          processedAnyLine = true;
          const line = buffer.substring(0, newlineIndex).replace(/\r$/, ""); // 移除可能的 \r
          const lineEndLength = buffer[newlineIndex] === "\r" && buffer[newlineIndex + 1] === "\n" ? 2 : 1;
          buffer = buffer.substring(newlineIndex + lineEndLength);
          console.log("前端: 处理行，长度:", line.length, "内容:", line.substring(0, Math.min(100, line.length)), "是否以event开头:", line.startsWith("event: "), "是否以data开头:", line.startsWith("data: "));
          
          if (line.startsWith("event: ")) {
            // 保存事件类型
            currentEventType = line.slice(7).trim();
            console.log("前端: 设置事件类型:", currentEventType);
            // 如果有待处理的数据，先处理它（上一个事件的数据）
            if (pendingData.trim()) {
              try {
                const event: StreamEvent = JSON.parse(pendingData.trim());
                processEvent(event);
                pendingData = "";
              } catch (e) {
                console.error("前端: 解析待处理数据失败:", e, "数据:", pendingData.substring(0, 100));
                pendingData = "";
              }
            }
          } else if (line.startsWith("data: ")) {
            console.log("前端: 检测到data行，完整内容:", line);
            const data = line.slice(6);
            // 累积数据（可能跨多行）
            if (pendingData === "") {
              pendingData = data;
            } else {
              pendingData += "\n" + data;
            }
            console.log("前端: 累积data，pendingData长度:", pendingData.length, "预览:", pendingData.substring(0, Math.min(100, pendingData.length)));
            
            // 尝试解析JSON
            try {
              const trimmed = pendingData.trim();
              console.log("前端: 尝试解析JSON，长度:", trimmed.length, "内容:", trimmed.substring(0, Math.min(100, trimmed.length)));
              const event: StreamEvent = JSON.parse(trimmed);
              console.log("前端: JSON解析成功！事件类型:", event.type, "完整事件:", JSON.stringify(event));
              processEvent(event);
              pendingData = ""; // 清空已处理的数据
            } catch (e) {
              // JSON不完整，继续累积
              const errorMsg = e instanceof Error ? e.message : String(e);
              console.log("前端: JSON不完整，继续累积，当前长度:", pendingData.length, "错误:", errorMsg);
            }
          } else if (line.trim() === "") {
            // 空行表示事件结束，处理pendingData
            if (pendingData.trim()) {
              try {
                const event: StreamEvent = JSON.parse(pendingData.trim());
                console.log("前端: 空行时解析成功，事件类型:", event.type);
                processEvent(event);
                pendingData = "";
              } catch (e) {
                console.error("前端: 空行时解析数据失败:", e, "数据:", pendingData.substring(0, 100));
                // 如果解析失败，清空pendingData，避免阻塞后续事件
                pendingData = "";
              }
            }
            currentEventType = ""; // 清空事件类型
          }
        }
      }
    } catch (error) {
      console.error("前端: 处理流式响应时出错:", error);
      if (error instanceof Error) {
        console.error("前端: 错误详情:", error.message, error.stack);
      }
      setMessages((prev) =>
        prev.map((msg) =>
          msg.id === assistantMsgId
            ? { ...msg, text: BACKEND_ERROR_MESSAGE + (error instanceof Error ? ` (${error.message})` : "") }
            : msg,
        ),
      );
    } finally {
      setLoading(false);
      setStreamingMessageId(null);
      setCurrentToolCall(null);
    }
  }

  function handleKeyDown(event: React.KeyboardEvent<HTMLTextAreaElement>) {
    if (event.key === "Enter" && !event.shiftKey) {
      event.preventDefault();
      void handleSend();
    }
  }

  return (
    <section className="flex w-full h-full flex-col bg-[#E0F2FE]">
      <div className="p-2 text-xs text-neutral-700">对话区（MCP 客户端入口）</div>

      <div className="flex flex-1 gap-2 p-2">
        <div className="flex flex-1 flex-col rounded bg-white/60 p-2 text-xs text-neutral-800">
          <div className="mb-1 text-[11px] font-medium text-neutral-600">
            对话记录
          </div>
          <div className="flex-1 space-y-1 overflow-auto">
            {messages.map((msg) => (
              <div key={msg.id} className="whitespace-pre-wrap">
                <span className="mr-1 font-semibold">
                  {msg.role === "user" ? "你" : "助手"}：
                </span>
                <span>{msg.text || (msg.role === "assistant" && loading ? "思考中..." : "")}</span>
                {msg.toolCalls && msg.toolCalls.length > 0 && (
                  <div className="mt-1 space-y-0.5">
                    {msg.toolCalls.map((toolCall, idx) => (
                      <div
                        key={idx}
                        className="text-[10px] text-blue-600 bg-blue-50 px-1 py-0.5 rounded"
                      >
                        调用工具: {toolCall.toolGroup} - {toolCall.toolName}
                      </div>
                    ))}
                  </div>
                )}
              </div>
            ))}
            {currentToolCall && (
              <div className="text-[10px] text-blue-600 bg-blue-50 px-1 py-0.5 rounded animate-pulse">
                正在调用: {currentToolCall.toolGroup} - {currentToolCall.toolName}
              </div>
            )}
            {messages.length === 0 && (
              <div className="text-[11px] text-neutral-400">
                请输入问题，开始与本地大模型对话（后端：Ollama + MCP）。
              </div>
            )}
          </div>
        </div>

        <div className="w-64 rounded bg-white/60 p-2 text-xs text-neutral-800">
          <div className="mb-1 text-[11px] font-medium text-neutral-600">
            图表区域（MCP 图表）
          </div>
          <div className="h-40">
            <ChartPanel chart={chart} />
          </div>
        </div>
      </div>

      <div className="border-t border-[#BFDBFE] bg-[#DBEAFE] p-2">
        <div className="flex items-end gap-2">
          <textarea
            className="h-16 flex-1 resize-none rounded border border-[#93C5FD] bg-white px-2 py-1 text-xs outline-none"
            placeholder="输入问题，回车发送（Shift+Enter 换行）"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
          />
          <button
            type="button"
            className="h-8 w-20 rounded bg-[#2563EB] text-[11px] font-medium text-white disabled:bg-[#93C5FD]"
            onClick={() => void handleSend()}
            disabled={loading || !input.trim()}
          >
            {loading ? "发送中…" : "发送"}
          </button>
        </div>
      </div>
    </section>
  );
}

