import { AppShell } from "@/components/layout/AppShell";
import { ChatPanel } from "@/components/chat/ChatPanel";
import { ChartPanel } from "@/components/chart/ChartPanel";
import { GraphPanel } from "@/components/graph/GraphPanel";

export default function ChatPage() {
  return (
    <AppShell>
      <div className="grid h-[calc(100vh-4rem)] gap-4 md:grid-cols-[minmax(0,2fr)_minmax(0,1.2fr)]">
        <ChatPanel />
        <div className="flex flex-col gap-4">
          <ChartPanel />
          <GraphPanel />
        </div>
      </div>
    </AppShell>
  );
}

