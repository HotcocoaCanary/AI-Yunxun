"use client";

import { useEffect, useRef } from "react";
import * as echarts from "echarts";

export type GraphNode = {
  id: string;
  label: string;
  type?: string;
};

export type GraphEdge = {
  id: string;
  source: string;
  target: string;
  label?: string;
};

export type GraphData = {
  nodes: GraphNode[];
  edges: GraphEdge[];
};

type GraphPanelProps = {
  data: GraphData;
};

/**
 * 使用 ECharts 绘制力导向图，可拖拽、缩放等交互。
 * 当前主要作为 Neo4j 结果的预览面板。
 */
export function GraphPanel({ data }: GraphPanelProps) {
  const containerRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    if (!containerRef.current) return;

    const chart = echarts.init(containerRef.current);

    // 根据节点类型生成类别，后续可以在 option 中使用不同颜色等
    const categories = Array.from(
      new Set(data.nodes.map((n) => n.type ?? "default")),
    ).map((name) => ({ name }));

    const option = {
      tooltip: {
        formatter: (params: unknown) => {
          const p = params as {
            dataType?: string;
            data?: { label?: string; name?: string; _labelText?: string };
          };

          if (p.dataType === "node") {
            return p.data?.label ?? p.data?.name ?? "节点";
          }
          if (p.dataType === "edge") {
            return p.data?._labelText ?? p.data?.name ?? "关系";
          }
          return "";
        },
      },
      animationDuration: 300,
      animationDurationUpdate: 300,
      series: [
        {
          type: "graph",
          layout: "force",
          roam: true,
          draggable: true,
          focusNodeAdjacency: true,
          symbolSize: 26,
          categories,
          force: {
            repulsion: 260,
            edgeLength: 80,
          },
          label: {
            show: true,
            formatter: (p: { data?: { label?: string }; name?: string }) =>
              p.data?.label ?? p.name ?? "",
            fontSize: 10,
          },
          edgeSymbol: ["circle", "arrow"],
          edgeSymbolSize: [4, 6],
          lineStyle: {
            width: 1,
            opacity: 0.6,
            color: "#A855F7",
          },
          data: data.nodes.map((n) => ({
            id: n.id,
            name: n.label,
            label: n.label,
            category: n.type ?? "default",
          })),
          links: data.edges.map((e) => ({
            source: e.source,
            target: e.target,
            _labelText: e.label,
            label: e.label
              ? {
                  show: true,
                  formatter: e.label,
                  fontSize: 9,
                  color: "#6B21A8",
                }
              : { show: false },
          })),
        },
      ],
    } as echarts.EChartsCoreOption;

    chart.setOption(option);

    const handleResize = () => {
      chart.resize();
    };

    window.addEventListener("resize", handleResize);

    return () => {
      window.removeEventListener("resize", handleResize);
      chart.dispose();
    };
  }, [data]);

  return (
    <section className="flex w-full h-full flex-col bg-[#EDE9FE] p-2">
      <div className="mb-2 text-xs font-semibold text-neutral-900">
        图谱组件 GraphPanel（ECharts 力导向图）
      </div>
      <div ref={containerRef} className="flex-1 rounded bg-white/60" />
    </section>
  );
}

