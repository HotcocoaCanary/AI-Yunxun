import { ChatPanel } from "@/components/chat/ChatPanel";
import { GraphPanel, type GraphData } from "@/components/graph/GraphPanel";

const mockGraphData: GraphData = {
  nodes: [
    { id: "q1", label: "问题：销售趋势", type: "query" },
    { id: "ds1", label: "数据集：sales_2024", type: "dataset" },
    { id: "m1", label: "图表：销售折线图", type: "chart" },
    { id: "dim1", label: "维度：日期", type: "dimension" },
    { id: "mea1", label: "指标：销售额", type: "metric" },
  ],
  edges: [
    { id: "e1", source: "q1", target: "ds1", label: "分析" },
    { id: "e2", source: "ds1", target: "m1", label: "生成图表" },
    { id: "e3", source: "ds1", target: "dim1", label: "按" },
    { id: "e4", source: "ds1", target: "mea1", label: "统计" },
  ],
};

export default function ChatPage() {
  return (
    <div className="flex flex-1 h-screen m-0 p-4 gap-4">
      <div className="flex-1 h-full">
        <ChatPanel />
      </div>
      <div className="w-80 h-full shrink-0">
        <GraphPanel data={mockGraphData} />
      </div>
    </div>
  );
}

