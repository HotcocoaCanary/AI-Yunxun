/**
 * 侧区容器：工具状态 + 日志 + 图表区。
 * 与 doc/nextjs-client-architecture.md 2.2 一致。
 * 数据由父组件通过 props 传入（来自 SSE 的 status、tool_log、chart）。
 */

"use client";

import React from "react";
import ToolStatus from "./ToolStatus";
import ToolLogs from "./ToolLogs";
import ChartPanel from "./ChartPanel";
import type { ChartPayload } from "./ChartPanel";

export interface SidePanelProps {
  status?: string;
  logs?: string[];
  chart?: ChartPayload | null;
  /** 直接调用图谱接口加载示例图（不经过大模型） */
  onLoadSampleGraph?: () => void;
}

export default function SidePanel({
  status = "—",
  logs = [],
  chart = null,
  onLoadSampleGraph,
}: SidePanelProps) {
  return (
    <div style={{ display: "flex", flexDirection: "column", height: "100%" }}>
      <ToolStatus status={status} />
      {onLoadSampleGraph && (
        <div style={{ padding: "8px 12px", borderBottom: "1px solid #e0e0e0" }}>
          <button
            type="button"
            onClick={onLoadSampleGraph}
            style={{
              padding: "6px 12px",
              fontSize: 12,
              border: "1px solid #666",
              borderRadius: 6,
              cursor: "pointer",
              background: "#fff",
            }}
          >
            加载示例关系图
          </button>
        </div>
      )}
      <ToolLogs logs={logs} />
      <div style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
        <ChartPanel chart={chart} width={340} height={280} />
      </div>
    </div>
  );
}
