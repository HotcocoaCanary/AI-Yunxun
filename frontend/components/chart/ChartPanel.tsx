"use client";

/**
 * Placeholder for chart gallery / preview area.
 * 将来这里会根据 MCP generate_chart 的返回渲染 ECharts 图表。
 */
export function ChartPanel() {
  return (
    <section className="flex h-full flex-col rounded-2xl border border-dashed border-amber-200 bg-amber-50/40 p-4">
      <h2 className="text-xs font-semibold text-amber-800">图表预览</h2>
      <p className="mt-1 text-xs text-amber-700/80">
        ChartResponse.chartSpec &amp; data 将在这里可视化；当前仅为占位组件。
      </p>
    </section>
  );
}

