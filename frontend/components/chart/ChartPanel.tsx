"use client";

import { useEffect, useRef } from "react";
import * as echarts from "echarts";

export type ChartResponse = {
  chartType?: string;
  engine?: string;
  title?: string;
  description?: string;
  chartSpec?: echarts.EChartsOption;
  data?: Array<Record<string, unknown>>;
  insightSummary?: string;
  insightBullets?: string[];
};

type ChartPanelProps = {
  chart: ChartResponse | null;
};

/**
 * Chart preview area backed by ECharts.
 * Receives a ChartResponse (usually produced by the MCP chart tool)
 * and renders chartSpec if the engine is "echarts".
 */
export function ChartPanel({ chart }: ChartPanelProps) {
  const containerRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    if (!containerRef.current) return;

    const instance = echarts.init(containerRef.current);

    if (chart && chart.engine === "echarts" && chart.chartSpec) {
      instance.setOption(chart.chartSpec);
    } else {
      instance.clear();
    }

    const handleResize = () => {
      instance.resize();
    };

    window.addEventListener("resize", handleResize);

    return () => {
      window.removeEventListener("resize", handleResize);
      instance.dispose();
    };
  }, [chart]);

  return (
    <section className="flex w-full h-full flex-col bg-[#FEF3C7] p-2">
      <div className="mb-1 text-[11px] font-medium text-neutral-700">
        图表组件 ChartPanel（ECharts）
      </div>
      <div ref={containerRef} className="flex-1 rounded bg-white/70" />
      {chart?.insightSummary && (
        <div className="mt-1 text-[11px] text-neutral-700">
          {chart.insightSummary}
        </div>
      )}
    </section>
  );
}

