import type { ReactNode } from "react";

type ChatLayoutProps = {
  children: ReactNode;
};

export default function ChatLayout({ children }: ChatLayoutProps) {
  return (
    <div className="flex min-h-screen bg-white text-neutral-900 m-0">
      <aside className="hidden w-56 flex-col bg-neutral-200 md:flex m-0 p-0">
        <div className="text-sm font-semibold tracking-tight p-2">AI-Yunxun</div>
        <p className="text-xs px-2">图问答 · 图表分析 · MCP</p>
        <nav className="text-sm px-2">
          <div>工作台</div>
          <div>聊天 / 图表（待实现）</div>
        </nav>
      </aside>

      <div className="flex min-h-screen flex-1 flex-col bg-white m-0">
        <header className="flex h-14 items-center border-b border-neutral-200 bg-white px-4">
          <div className="text-sm font-medium">智能图问分析工作台</div>
        </header>

        <main className="flex-1 overflow-auto m-0 p-0">{children}</main>
      </div>
    </div>
  );
}

