import { ChatCard } from "@/ui/components/chat/ChatCard";

export default function ChatPage() {
  return (
    <main className="app">
      <section className="app-shell">
        <aside className="sidebar">
          <div className="brand">
            <div className="brand-title">MCP Chat Console</div>
            <div className="brand-subtitle">
              Minimal workspace for multi-tool conversations.
            </div>
          </div>
          <div className="sidebar-meta">
            <div className="status-pill">
              <span className="status-dot" />
              Ready
            </div>
            <div className="sidebar-caption">Connected to MCP runtime</div>
          </div>
        </aside>
        <section className="app-content">
          <ChatCard />
        </section>
      </section>
    </main>
  );
}
