"use client";

import { useState } from "react";
import { ChatPanel } from "@/components/chat/ChatPanel";
import { GraphPanel, type GraphData } from "@/components/graph/GraphPanel";

export default function ChatPage() {
  const [graphData, setGraphData] = useState<GraphData | null>(null);

  return (
    <div className="flex flex-1 h-screen m-0 p-4 gap-4">
      <div className="flex-1 h-full">
        <ChatPanel onGraphChange={setGraphData} />
      </div>
      <div className="w-80 h-full shrink-0">
        {graphData ? (
          <GraphPanel data={graphData} />
        ) : (
          <div className="h-full rounded bg-[#F3E8FF] p-2 text-xs text-neutral-500">
            图谱区：当问题涉及图谱（例如“Bob 认识谁？”）并且模型返回 GRAPH_JSON
            数据时，这里会显示对应的 Neo4j 图谱。
          </div>
        )}
      </div>
    </div>
  );
}
