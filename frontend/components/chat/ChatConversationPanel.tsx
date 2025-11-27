'use client';
import { FormEvent, useMemo, useState } from "react";
import ReactMarkdown from "react-markdown";
import { ArrowUpRight, Loader2, Send, Square } from "lucide-react";
import { ChatMessage } from "@/types/chat";
import GraphCard from "../graph/GraphCard";
import ChartsPanel from "../chart/ChartsPanel";

type ChatConversationPanelProps = {
  messages: ChatMessage[];
  onSend: (text: string) => void;
  onStop: () => void;
  isStreaming: boolean;
};

const formatTime = (value?: string) => {
  if (!value) return "";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleString("zh-CN", {
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  });
};

export default function ChatConversationPanel({
  messages,
  onSend,
  onStop,
  isStreaming,
}: ChatConversationPanelProps) {
  const [value, setValue] = useState("");

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault();
    if (!value.trim()) return;
    onSend(value.trim());
    setValue("");
  };

  const hasMessages = useMemo(() => messages.length > 0, [messages]);

  return (
    <section className="chat-surface">
      <div className="chat-surface-header">
        <div>
          <p className="text-[11px] uppercase tracking-[0.08em] text-slate-400">对话</p>
          <p className="text-lg font-semibold text-slate-900">图谱驱动问答</p>
        </div>
        {isStreaming ? (
          <button type="button" className="btn btn-neutral" onClick={onStop}>
            <Square size={14} />
            停止
          </button>
        ) : null}
      </div>

      <div className="chat-messages">
        {!hasMessages ? (
          <div className="empty-state">
            输入问题或粘贴文本 / URL，智能体会结合图谱与工具给出答案。
          </div>
        ) : (
          messages.map((msg) => (
            <div key={msg.id} className={`message-row ${msg.role === "user" ? "user" : "ai"}`}>
              <article className={`chat-bubble ${msg.role === "assistant" ? "ai" : "user"}`}>
                <div className="message-meta">
                  <div className="flex items-center gap-2 text-xs font-semibold text-slate-600">
                    <span className={`message-badge ${msg.role === "assistant" ? "assistant" : "user"}`}>
                      {msg.role === "assistant" ? "AI" : "我"}
                    </span>
                    <span className="text-[11px] text-slate-500">{formatTime(msg.createdAt)}</span>
                  </div>
                  {msg.role === "assistant" && (msg.graph || msg.charts) ? (
                    <span className="inline-flex items-center gap-1 text-[11px] text-sky-600">
                      <ArrowUpRight size={12} />
                      图谱 / 图表
                    </span>
                  ) : null}
                </div>
                <div className="prose prose-slate max-w-none text-[15px] leading-relaxed">
                  <ReactMarkdown>{msg.content}</ReactMarkdown>
                </div>
                {msg.graph ? (
                  <div className="mt-3">
                    <GraphCard data={msg.graph} />
                  </div>
                ) : null}
                {msg.charts?.length ? (
                  <div className="mt-3">
                    <ChartsPanel charts={msg.charts} />
                  </div>
                ) : null}
              </article>
            </div>
          ))
        )}
      </div>

      <form onSubmit={handleSubmit} className="composer-bar">
        <textarea
          value={value}
          onChange={(e) => setValue(e.target.value)}
          placeholder="发送消息，或粘贴文本 / 链接（Shift+Enter 换行）"
          className="composer-input"
          onKeyDown={(e) => {
            if (e.key === "Enter" && !e.shiftKey) {
              e.preventDefault();
              handleSubmit(e as unknown as FormEvent);
            }
          }}
        />
        <button
          type="submit"
          className="composer-send"
          disabled={!value.trim() || isStreaming}
          aria-label="发送"
        >
          {isStreaming ? <Loader2 className="h-4 w-4 animate-spin" /> : <Send size={18} />}
        </button>
      </form>
    </section>
  );
}
