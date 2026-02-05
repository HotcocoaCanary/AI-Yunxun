import { ChatCard } from "@/ui/components/chat/ChatCard";

export default function ChatPage() {
  return (
    <main className="app">
      <section className="app-shell">
        <header className="app-header">
          <div className="brand">
            <div className="brand-title">MCP Chat Console</div>
            <div className="brand-subtitle">
              Minimal workspace for multi-tool conversations.
            </div>
          </div>
          <div className="status-pill">
            <span className="status-dot" />
            Ready
          </div>
        </header>
        <ChatCard />
      </section>
    </main>
  );
}
