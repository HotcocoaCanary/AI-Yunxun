"use client";

import { useState } from "react";
import { ChartPanel } from "@/components/chart/ChartPanel";
import type { GraphData } from "@/components/graph/GraphPanel";

type ChatMessage = {
  id: string;
  role: "user" | "assistant";
  text: string;
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

    try {
      const res = await fetch("/api/chat", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ message: trimmed }),
      });

      if (!res.ok) {
        throw new Error("Chat API error");
      }

      const data = (await res.json()) as {
        reply?: string;
        graphJson?: string | null;
      };
      const replyText = data.reply ?? BACKEND_ERROR_MESSAGE;

      if (onGraphChange) {
        if (data.graphJson) {
          try {
            const parsed = JSON.parse(data.graphJson) as GraphData;
            onGraphChange(parsed);
          } catch {
            onGraphChange(null);
          }
        } else {
          onGraphChange(null);
        }
      }

      const assistantMsg: ChatMessage = {
        id: crypto.randomUUID(),
        role: "assistant",
        text: replyText,
      };
      setMessages((prev) => [...prev, assistantMsg]);
    } catch {
      const errorMsg: ChatMessage = {
        id: crypto.randomUUID(),
        role: "assistant",
        text: BACKEND_ERROR_MESSAGE,
      };
      setMessages((prev) => [...prev, errorMsg]);
    } finally {
      setLoading(false);
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
          <div className="mb-1 text-[11px] font-medium text-neutral-600">对话记录</div>
          <div className="flex-1 space-y-1 overflow-auto">
            {messages.map((msg) => (
              <div key={msg.id} className="whitespace-pre-wrap">
                <span className="mr-1 font-semibold">
                  {msg.role === "user" ? "你" : "助手"}：
                </span>
                <span>{msg.text}</span>
              </div>
            ))}
            {messages.length === 0 && (
              <div className="text-[11px] text-neutral-400">
                请输入问题，开始与本地大模型对话（后端：Ollama + MCP）。
              </div>
            )}
          </div>
        </div>

        <div className="w-64 rounded bg-white/60 p-2 text-xs text-neutral-800">
          <div className="mb-1 text-[11px] font-medium text-neutral-600">图表占位（未来嵌入 MCP 图表）</div>
          <div className="h-40">
            <ChartPanel />
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

