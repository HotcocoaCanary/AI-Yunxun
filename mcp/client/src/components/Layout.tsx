/**
 * 整体布局：Header + 主区（对话）+ 侧区（工具状态、日志、图表）。
 * 与 doc/nextjs-client-architecture.md 2.1 一致。
 */

import React from "react";

export default function Layout({
  children,
  side,
}: {
  children: React.ReactNode;
  side: React.ReactNode;
}) {
  return (
    <div style={{ display: "flex", flexDirection: "column", height: "100vh" }}>
      <header
        style={{
          padding: "12px 16px",
          borderBottom: "1px solid #e0e0e0",
          background: "#fafafa",
          fontWeight: 600,
        }}
      >
        MCP 对话与图谱
      </header>
      <div style={{ display: "flex", flex: 1, overflow: "hidden" }}>
        <main style={{ flex: 1, overflow: "auto", minWidth: 0 }}>{children}</main>
        <aside
          style={{
            width: 360,
            borderLeft: "1px solid #e0e0e0",
            display: "flex",
            flexDirection: "column",
            overflow: "hidden",
          }}
        >
          {side}
        </aside>
      </div>
    </div>
  );
}
