/**
 * 图表区：接收 SSE 的 chart 或直接调用接口的返回。
 * 若为 option，用 ECharts + echarts-gl 根据 series[].type 渲染 graph/graphGL；
 * 若为 image（base64/data URL），用 <img> 展示，无需 ECharts。
 */

"use client";

import React, { useEffect, useRef, useCallback } from "react";
import * as echarts from "echarts";
import "echarts-gl";

export type ChartPayload =
  | { type: "option"; option: Record<string, unknown> }
  | { type: "image"; data: string; mimeType: string };

export interface ChartPanelProps {
  chart: ChartPayload | null;
  width?: number;
  height?: number;
}

export default function ChartPanel({ chart, width = 340, height = 280 }: ChartPanelProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const chartRef = useRef<echarts.ECharts | null>(null);

  const renderOption = useCallback(
    (option: Record<string, unknown>) => {
      if (!containerRef.current) return;
      if (!chartRef.current) {
        chartRef.current = echarts.init(containerRef.current);
      }
      chartRef.current.setOption(option, true);
    },
    []
  );

  useEffect(() => {
    if (!chart) {
      if (chartRef.current) {
        chartRef.current.clear();
      }
      return;
    }

    if (chart.type === "image") {
      if (chartRef.current) {
        chartRef.current.dispose();
        chartRef.current = null;
      }
      return;
    }

    renderOption(chart.option);
  }, [chart, renderOption]);

  useEffect(() => {
    return () => {
      if (chartRef.current) {
        chartRef.current.dispose();
        chartRef.current = null;
      }
    };
  }, []);

  if (!chart) {
    return (
      <div
        style={{
          flex: 1,
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          color: "#888",
          fontSize: 13,
          padding: 12,
        }}
      >
        图表区：对话触发出图或直接调用 /api/tools/echart/graph 展示
      </div>
    );
  }

  if (chart.type === "image") {
    const src =
      chart.mimeType === "image/svg+xml"
        ? `data:image/svg+xml;base64,${chart.data}`
        : `data:${chart.mimeType};base64,${chart.data}`;
    return (
      <div style={{ flex: 1, padding: 12, overflow: "auto" }}>
        <img
          src={src}
          alt="chart"
          style={{ maxWidth: "100%", height: "auto", display: "block" }}
        />
      </div>
    );
  }

  return (
    <div
      ref={containerRef}
      style={{ width: "100%", height: "100%", minHeight: height }}
    />
  );
}
