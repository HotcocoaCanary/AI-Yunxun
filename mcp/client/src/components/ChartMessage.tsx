"use client";

import React, { useEffect, useRef } from "react";
import * as echarts from "echarts";
import type { ChartPayload } from "@/lib/chat-stream";

export default function ChartMessage({ chart }: { chart: ChartPayload }) {
  const containerRef = useRef<HTMLDivElement>(null);
  const instanceRef = useRef<echarts.ECharts | null>(null);

  useEffect(() => {
    if (!containerRef.current) return;

    if (chart.type === "image") {
      if (instanceRef.current) {
        instanceRef.current.dispose();
        instanceRef.current = null;
      }
      return;
    }

    if (!instanceRef.current) {
      instanceRef.current = echarts.init(containerRef.current);
    }
    instanceRef.current.setOption(chart.option, true);
  }, [chart]);

  useEffect(() => {
    const el = containerRef.current;
    const instance = instanceRef.current;
    if (!el || !instance) return;
    const observer = new ResizeObserver(() => {
      instance.resize();
    });
    observer.observe(el);
    return () => observer.disconnect();
  }, []);

  useEffect(() => {
    return () => {
      if (instanceRef.current) {
        instanceRef.current.dispose();
        instanceRef.current = null;
      }
    };
  }, []);

  if (chart.type === "image") {
    const src =
      chart.mimeType === "image/svg+xml"
        ? `data:image/svg+xml;base64,${chart.data}`
        : `data:${chart.mimeType};base64,${chart.data}`;
    return (
      <img
        src={src}
        alt="chart"
        style={{ maxWidth: "100%", height: "auto", display: "block" }}
      />
    );
  }

  return (
    <div
      ref={containerRef}
      style={{ width: "100%", height: 320, minHeight: 240 }}
    />
  );
}