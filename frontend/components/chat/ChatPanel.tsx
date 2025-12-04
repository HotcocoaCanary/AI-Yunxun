"use client";

/**
 * Placeholder for the main chat + MCP interaction area.
 * Later we will wire:
 * - LLM message list
 * - MCP tool calling (SSE)
 * - Chart and graph previews
 */
export function ChatPanel() {
  return (
    <section className="flex h-full flex-col rounded-2xl border border-zinc-200 bg-white/90 p-4 shadow-sm">
      <header className="mb-3 flex items-center justify-between">
        <div>
          <h2 className="text-sm font-semibold text-zinc-900">对话区域</h2>
          <p className="mt-1 text-xs text-zinc-500">
            这里以后会展示 LLM 对话、MCP 工具调用计划，以及返回的图表和图谱。
          </p>
        </div>
      </header>

      <div className="flex flex-1 flex-col items-center justify-center text-xs text-zinc-400">
        <p>聊天 UI 尚未接入，现在只是前端架构占位。</p>
      </div>
    </section>
  );
}

