import React from "react";

export default function Layout({ children }: { children: React.ReactNode }) {
  return (
    <div style={{ display: "flex", flexDirection: "column", height: "100vh" }}>
      <header
        style={{
          padding: "14px 18px",
          borderBottom: "1px solid #e5e7eb",
          background: "rgba(255,255,255,0.8)",
          backdropFilter: "blur(8px)",
          fontWeight: 600,
        }}
      >
        MCP Chat
      </header>
      <main style={{ flex: 1, overflow: "hidden" }}>{children}</main>
    </div>
  );
}