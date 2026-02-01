/**
 * 侧区：简要日志列表，最近 N 条，来自 SSE 的 tool_log 或 MCP 日志解析。
 */

"use client";

import React from "react";

export interface ToolLogsProps {
  logs: string[];
  maxLines?: number;
}

export default function ToolLogs({ logs, maxLines = 20 }: ToolLogsProps) {
  const show = logs.slice(-maxLines);

  return (
    <div
      style={{
        padding: 12,
        borderBottom: "1px solid #e0e0e0",
        fontSize: 12,
        maxHeight: 120,
        overflow: "auto",
        background: "#fafafa",
      }}
    >
      <strong>日志</strong>
      <ul style={{ margin: "4px 0 0", paddingLeft: 18 }}>
        {show.map((line, i) => (
          <li key={i} style={{ wordBreak: "break-word" }}>
            {line}
          </li>
        ))}
        {show.length === 0 && <li style={{ color: "#888" }}>—</li>}
      </ul>
    </div>
  );
}
