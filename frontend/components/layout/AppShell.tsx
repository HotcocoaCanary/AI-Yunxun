"use client";

import type { ReactNode } from "react";

type AppShellProps = {
  children: ReactNode;
};

/**
 * Top-level application shell layout:
 * - left sidebar for navigation / tool status
 * - header bar
 * - main content area for chat, charts, etc.
 */
export function AppShell({ children }: AppShellProps) {
  return (
    <div className="flex min-h-screen bg-background text-foreground">
      <aside className="hidden w-56 flex-col border-r border-zinc-200 bg-white/60 px-4 py-6 md:flex">
        <div className="text-sm font-semibold tracking-tight text-zinc-900">
          AI-Yunxun
        </div>
        <p className="mt-2 text-xs text-zinc-500">
          图谱问答 · 图表分析 · MCP
        </p>
        <nav className="mt-6 space-y-2 text-sm text-zinc-700">
          <div className="rounded-md px-2 py-1.5 bg-zinc-100/80">
            工作台
          </div>
          <div className="rounded-md px-2 py-1.5 text-zinc-400">
            聊天 / 图表（待实现）
          </div>
        </nav>
      </aside>

      <div className="flex min-h-screen flex-1 flex-col bg-zinc-50">
        <header className="flex h-14 items-center border-b border-zinc-200 bg-white/70 px-4 backdrop-blur">
          <div className="text-sm font-medium text-zinc-800">
            智能图谱分析工作台
          </div>
        </header>

        <main className="flex-1 overflow-auto p-4">{children}</main>
      </div>
    </div>
  );
}

