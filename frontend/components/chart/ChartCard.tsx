'use client';
import ReactECharts from "echarts-for-react";
import { ChartSpec } from "@/types/chart";

type ChartCardProps = {
  spec: ChartSpec;
};

function buildOption(spec: ChartSpec) {
  const base = {
    title: { text: spec.title, left: "center", textStyle: { fontSize: 14 } },
    grid: { left: 40, right: 20, top: 50, bottom: 30 },
    tooltip: {},
    dataset: { source: spec.data },
  };

  if (spec.type === "pie") {
    return {
      ...base,
      series: [
        {
          type: "pie",
          radius: ["30%", "60%"],
          label: { formatter: "{b}: {d}%" },
        },
      ],
    };
  }

  if (spec.type === "line") {
    return {
      ...base,
      xAxis: { type: "category" },
      yAxis: { type: "value" },
      series: [{ type: "line", smooth: true }],
    };
  }

  if (spec.type === "bar") {
    return {
      ...base,
      xAxis: { type: "category" },
      yAxis: { type: "value" },
      series: [{ type: "bar", barWidth: "40%" }],
    };
  }

  return base;
}

export default function ChartCard({ spec }: ChartCardProps) {
  if (spec.type === "table") {
    const columns = Object.keys(spec.data?.[0] || {});
    return (
      <div className="rounded-2xl border border-slate-100 bg-white/90 p-4 shadow-sm">
        <h3 className="mb-3 text-base font-semibold text-slate-900">{spec.title}</h3>
        <div className="overflow-auto rounded-xl border border-slate-100">
          <table className="min-w-full text-sm text-slate-700">
            <thead className="bg-slate-50">
              <tr>
                {columns.map((col) => (
                  <th key={col} className="px-3 py-2 text-left font-semibold">
                    {col}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {spec.data.map((row, idx) => (
                <tr key={spec.id + idx} className="border-t border-slate-100">
                  {columns.map((col) => (
                    <td key={col} className="px-3 py-2">
                      {String(row[col] ?? "")}
                    </td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    );
  }

  return (
    <div className="rounded-2xl border border-slate-100 bg-white/90 p-4 shadow-sm">
      <ReactECharts option={buildOption(spec)} style={{ height: 240 }} />
    </div>
  );
}
