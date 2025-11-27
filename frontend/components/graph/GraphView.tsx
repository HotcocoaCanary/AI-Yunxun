/* eslint-disable react/no-unstable-nested-components */
'use client';
import ReactECharts from "echarts-for-react";
import { GraphData, GraphNode } from "@/types/graph";

type GraphViewProps = {
  data: GraphData;
  height?: number | string;
  interactive?: boolean;
  onNodeClick?: (node: GraphNode) => void;
  onNodeDoubleClick?: (node: GraphNode) => void;
};

export default function GraphView({
  data,
  height = 380,
  interactive = true,
  onNodeClick,
  onNodeDoubleClick,
}: GraphViewProps) {
  const option = {
    tooltip: {
      trigger: "item",
      formatter: (params: any) => {
        if (params.dataType === "edge") {
          return `${params.data.type || "relation"}<br/>${params.data.source} → ${params.data.target}`;
        }
        return `${params.data.label || params.data.id}`;
      },
    },
    series: [
      {
        type: "graph",
        layout: "force",
        roam: interactive,
        draggable: interactive,
        focusNodeAdjacency: true,
        label: { show: true, color: "#0f172a", fontWeight: 600 },
        force: { repulsion: 280, edgeLength: 140 },
        data: data.nodes.map((node) => ({
          id: node.id,
          name: node.label,
          label: node.label,
          value: node.properties,
          symbolSize: 36,
          itemStyle: { color: "#0ea5e9" },
        })),
        links: data.edges.map((edge) => ({
          id: edge.id,
          source: edge.source,
          target: edge.target,
          type: edge.type,
          lineStyle: { color: "#94a3b8" },
        })),
      },
    ],
  };

  return (
    <div className="w-full overflow-hidden rounded-2xl border border-slate-100 bg-white/90 shadow-sm">
      <ReactECharts
        option={option}
        style={{ height, width: "100%" }}
        onEvents={{
          click: (params) => {
            if (params.dataType === "node" && onNodeClick) {
              const node = data.nodes.find((item) => item.id === params.data.id);
              if (node) onNodeClick(node);
            }
          },
          dblclick: (params) => {
            if (params.dataType === "node" && onNodeDoubleClick) {
              const node = data.nodes.find((item) => item.id === params.data.id);
              if (node) onNodeDoubleClick(node);
            }
          },
        }}
      />
    </div>
  );
}
