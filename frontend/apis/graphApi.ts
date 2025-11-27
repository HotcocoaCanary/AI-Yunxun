import { GraphData } from "@/types/graph";

export const GraphApi = {
  async getGlobalOverview(): Promise<GraphData> {
    const res = await fetch("/api/graph/overview", { cache: "no-store" });
    if (!res.ok) {
      throw new Error("加载图谱总览失败");
    }
    return res.json();
  },
  async expandNode(nodeId: string, limit = 20): Promise<GraphData> {
    const res = await fetch("/api/graph/expand", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ nodeId, limit }),
    });
    if (!res.ok) {
      throw new Error("扩展节点失败");
    }
    return res.json();
  },
};
