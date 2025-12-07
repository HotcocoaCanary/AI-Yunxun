import { ChatPanel } from "@/components/chat/ChatPanel";
import { ChartPanel } from "@/components/chart/ChartPanel";
import { GraphPanel } from "@/components/graph/GraphPanel";

export default function ChatPage() {
  return (
    <div className="grid h-full md:grid-cols-[minmax(0,2fr)_minmax(0,1.2fr)] m-0 p-0">
      <ChatPanel />
      <div className="flex flex-col m-0 p-0">
        <ChartPanel />
        <GraphPanel />
      </div>
    </div>
  );
}
