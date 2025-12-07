import type { ReactNode } from "react";

type ChatLayoutProps = {
  children: ReactNode;
};

export default function ChatLayout({ children }: ChatLayoutProps) {
  return (
    <div className="flex min-h-screen bg-white text-neutral-900 m-0">
      <aside className="hidden w-64 flex-col bg-[#F4F4F5] md:flex m-0 p-0">
        <div className="p-3 text-xs text-neutral-700">消息列表 MessageListPanel</div>
      </aside>

      <main className="flex flex-1 overflow-auto m-0 p-0">{children}</main>
    </div>
  );
}

