import { ChatPanel } from "@/components/chat/ChatPanel";
import { GraphPanel } from "@/components/graph/GraphPanel";

export default function ChatPage() {
  return (
    <div className="flex flex-1 h-screen m-0 p-0">
      <div className="flex-1 h-full">
        <ChatPanel />
      </div>
      <div className="w-80 h-full shrink-0">
        <GraphPanel />
      </div>
    </div>
  );
}
