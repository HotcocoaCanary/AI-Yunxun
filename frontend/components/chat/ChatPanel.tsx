"use client";

import { ChartPanel } from "@/components/chart/ChartPanel";

/**
 * Placeholder for the main chat + MCP interaction area.
 * Later we will wire:
 * - LLM message list
 * - MCP tool calling (SSE)
 * - Chart and graph previews
 */
export function ChatPanel() {
  return (
    <section className="flex w-full h-full flex-col bg-[#E0F2FE]">
      <div className="p-2 text-xs text-neutral-700">对话区 ChatPanel（内嵌图表）</div>
      <div className="flex flex-1 items-center justify-center">
        <ChartPanel />
      </div>
    </section>
  );
}

