/**
 * 首页：主区（对话列表 + 输入框）+ 侧区（工具状态、日志、图表）。
 * 对话提交时 POST /api/chat，消费 SSE，按 event 类型更新消息、状态与图表；
 * 可直接调用 /api/tools/echart/graph 或 /graph-gl 展示图谱（见侧栏说明或单独入口）。
 */

"use client";

import React, { useState, useCallback } from "react";
import Layout from "@/components/Layout";
import ChatPanel from "@/components/ChatPanel";
import SidePanel from "@/components/SidePanel";
import type { ChartPayload } from "@/components/ChartPanel";

export default function Home() {
  const [status, setStatus] = useState("—");
  const [logs, setLogs] = useState<string[]>([]);
  const [chart, setChart] = useState<ChartPayload | null>(null);

  const onStatus = useCallback((s: string) => setStatus(s), []);
  const onToolLog = useCallback((log: string) => setLogs((prev) => [...prev.slice(-19), log]), []);
  const onChart = useCallback((payload: ChartPayload) => setChart(payload), []);

  const loadSampleGraph = useCallback(async () => {
    setStatus("加载中…");
    try {
      const res = await fetch("/api/tools/echart/graph", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          title: "示例关系图",
          data: {
            nodes: [
              { id: "a", name: "A", category: "类型1" },
              { id: "b", name: "B", category: "类型1" },
              { id: "c", name: "C", category: "类型2" },
            ],
            edges: [
              { source: "a", target: "b", value: 1 },
              { source: "b", target: "c", value: 1 },
            ],
          },
          layout: "force",
          outputType: "option",
        }),
      });
      if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        setChart(null);
        setStatus(`失败: ${(err as { detail?: string }).detail ?? res.status}`);
        return;
      }
      const result = (await res.json()) as ChartPayload;
      setChart(result);
      setStatus("完成");
    } catch (e) {
      setStatus(`错误: ${e instanceof Error ? e.message : String(e)}`);
      setChart(null);
    }
  }, []);

  return (
    <Layout
      side={
        <SidePanel
          status={status}
          logs={logs}
          chart={chart}
          onLoadSampleGraph={loadSampleGraph}
        />
      }
    >
      <ChatPanel
        onStatus={onStatus}
        onChart={onChart}
        onToolLog={onToolLog}
      />
    </Layout>
  );
}
