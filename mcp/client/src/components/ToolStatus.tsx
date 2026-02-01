/**
 * 侧区：展示当前工具调用状态（调用中/成功/失败）。
 * 数据来自 SSE 的 status 或 tool_log。
 */

"use client";

import React from "react";

export interface ToolStatusProps {
  status?: string;
}

export default function ToolStatus({ status }: ToolStatusProps) {
  return (
    <div style={{ padding: 12, borderBottom: "1px solid #e0e0e0", fontSize: 13 }}>
      <strong>状态</strong>: {status ?? "—"}
    </div>
  );
}
