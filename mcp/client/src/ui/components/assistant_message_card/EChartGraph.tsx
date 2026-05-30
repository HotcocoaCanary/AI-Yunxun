"use client";

import { useEffect, useRef } from "react";
import * as echarts from "echarts";
import type { EChartsOption } from "echarts";

interface EChartGraphProps {
  option: EChartsOption;
}

const defaultColors = [
  "#4f46e5", "#06b6d4", "#10b981", "#f59e0b", "#ef4444",
  "#8b5cf6", "#ec4899", "#14b8a6", "#f97316", "#3b82f6",
];

function isGraphSeries(s: unknown): boolean {
  return (
    !!s &&
    typeof s === "object" &&
    (s as Record<string, unknown>).type === "graph"
  );
}

function sanitizeOption(option: EChartsOption): EChartsOption {
  const safe: Record<string, unknown> = {};

  const fontFamily =
    "-apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif";

  for (const [key, val] of Object.entries(option)) {
    switch (key) {
      case "grid": {
        const g = val as Record<string, unknown> | undefined;
        safe.grid = {
          left: "3%",
          right: "5%",
          bottom: "14%",
          top: "10%",
          containLabel: true,
          ...g,
        };
        break;
      }

      case "legend": {
        if (val === false || val === null) {
          safe.legend = undefined;
        } else if (val && typeof val === "object") {
          const l = val as Record<string, unknown>;
          safe.legend = {
            type: "scroll",
            bottom: 0,
            itemWidth: 12,
            itemHeight: 12,
            itemGap: 16,
            textStyle: { fontSize: 11, overflow: "truncate", width: 100 },
            ...l,
          };
        } else {
          safe.legend = val;
        }
        break;
      }

      case "tooltip": {
        const t = val as Record<string, unknown> | undefined;
        safe.tooltip = {
          confine: true,
          backgroundColor: "rgba(255,255,255,0.96)",
          borderColor: "#e0e3e8",
          borderWidth: 1,
          textStyle: { fontSize: 12, color: "#1a1b1e" },
          extraCssText:
            "max-width: 280px; white-space: normal; word-break: break-all; border-radius: 8px; box-shadow: 0 4px 16px rgba(0,0,0,0.1);",
          ...t,
        };
        break;
      }

      case "textStyle":
        safe.textStyle = {
          fontFamily,
          fontSize: 12,
          ...(val as Record<string, unknown>),
        };
        break;

      case "title": {
        if (val && typeof val === "object") {
          const t = val as Record<string, unknown>;
          safe.title = {
            textStyle: { fontSize: 15, fontWeight: 600, fontFamily },
            left: "center",
            top: 4,
            ...t,
          };
        } else {
          safe.title = val;
        }
        break;
      }

      case "xAxis": {
        const applyAxis = (ax: Record<string, unknown>) => {
          const existingLabel = ax?.axisLabel as Record<string, unknown> | undefined;
          const existingLine = ax?.axisLine as Record<string, unknown> | undefined;
          const existingSplitLine = ax?.splitLine as Record<string, unknown> | undefined;
          return {
            ...ax,
            axisLine: {
              lineStyle: { color: "#e0e3e8" },
              ...existingLine,
            },
            axisTick: { show: false, ...(ax?.axisTick as Record<string, unknown> || {}) },
            splitLine: { show: false, ...existingSplitLine },
            axisLabel: {
              fontSize: 11,
              fontFamily,
              color: "#6b7280",
              overflow: "truncate",
              width: 100,
              ...existingLabel,
            },
          };
        };
        if (Array.isArray(val)) {
          safe.xAxis = val.map((ax) =>
            applyAxis(typeof ax === "object" ? ax as Record<string, unknown> : {})
          );
        } else if (val && typeof val === "object") {
          safe.xAxis = applyAxis(val as Record<string, unknown>);
        } else {
          safe.xAxis = val;
        }
        break;
      }

      case "yAxis": {
        const applyAxis = (ay: Record<string, unknown>) => {
          const existingLabel = ay?.axisLabel as Record<string, unknown> | undefined;
          const existingSplitLine = ay?.splitLine as Record<string, unknown> | undefined;
          return {
            ...ay,
            axisLine: { show: false },
            axisTick: { show: false, ...(ay?.axisTick as Record<string, unknown> || {}) },
            splitLine: {
              lineStyle: { color: "#f0f0f3", type: "dashed" as const },
              ...existingSplitLine,
            },
            axisLabel: {
              fontSize: 11,
              fontFamily,
              color: "#6b7280",
              overflow: "truncate",
              width: 90,
              ...existingLabel,
            },
          };
        };
        if (Array.isArray(val)) {
          safe.yAxis = val.map((ay) =>
            applyAxis(typeof ay === "object" ? ay as Record<string, unknown> : {})
          );
        } else if (val && typeof val === "object") {
          safe.yAxis = applyAxis(val as Record<string, unknown>);
        } else {
          safe.yAxis = val;
        }
        break;
      }

      case "series": {
        if (Array.isArray(val)) {
          safe.series = (val as Record<string, unknown>[]).map((s, i) => {
            if (isGraphSeries(s)) {
              return {
                animationDuration: 800,
                animationEasing: "cubicOut" as const,
                ...s,
              };
            }

            const existingLabel = s?.label as Record<string, unknown> | undefined;
            const existingItemStyle = (s?.itemStyle as Record<string, unknown> | undefined) || {};
            const emphasis = (s?.emphasis as Record<string, unknown>) || { focus: "series" };

            return {
              animationDuration: 800,
              animationEasing: "cubicOut" as const,
              ...s,
              color:
                (s?.color as string) ||
                (i < defaultColors.length ? defaultColors[i] : undefined),
              emphasis: {
                focus: "series",
                ...emphasis,
              },
              itemStyle: {
                borderRadius: 4,
                borderWidth: 0,
                ...existingItemStyle,
              },
              label: existingLabel
                ? {
                    overflow: "truncate",
                    width: 80,
                    fontSize: 11,
                    fontFamily,
                    color: "#6b7280",
                    ...existingLabel,
                  }
                : existingLabel,
            };
          });
        } else {
          safe.series = val;
        }
        break;
      }

      case "color":
        safe.color = val && Array.isArray(val) && (val as unknown[]).length > 0
          ? val
          : defaultColors;
        break;

      case "backgroundColor":
        safe.backgroundColor = val || "#ffffff";
        break;

      default:
        safe[key] = val;
    }
  }

  if (!("color" in safe)) {
    safe.color = defaultColors;
  }

  return safe as EChartsOption;
}

export const EChartGraph = ({ option }: EChartGraphProps) => {
  const chartRef = useRef<HTMLDivElement>(null);
  const instanceRef = useRef<echarts.ECharts | null>(null);

  useEffect(() => {
    if (!chartRef.current) return;

    const chartInstance = echarts.init(chartRef.current);
    instanceRef.current = chartInstance;

    const safeOption = sanitizeOption(option);
    chartInstance.setOption(safeOption, { notMerge: true });

    const hasGraphSeries =
      Array.isArray(safeOption.series) &&
      safeOption.series.some((s) =>
        isGraphSeries(s)
      );

    if (hasGraphSeries) {
      chartInstance.setOption({
        tooltip: {
          formatter: (params: unknown) => {
            const p = params as {
              dataType?: string;
              name?: string;
              data?: { source?: string; target?: string; value?: unknown; sourceName?: string };
            };
            if (p.dataType === "edge" && p.data) {
              const src = p.data.source ?? "?";
              const tgt = p.data.target ?? "?";
              const val =
                p.data.value && typeof p.data.value === "string"
                  ? p.data.value.replace(/\n/g, "<br/>")
                  : p.data.value
                    ? String(p.data.value)
                    : "";
              return (
                "<b>" +
                src +
                " → " +
                tgt +
                "</b>" +
                (val
                  ? '<br/><span style="color:#6b7280">' + val + "</span>"
                  : "")
              );
            }
            const val =
              p.data?.value && typeof p.data.value === "string"
                ? p.data.value.replace(/\n/g, "<br/>")
                : p.data?.value
                  ? String(p.data.value)
                  : "";
            return (
              '<b style="font-size:14px">' +
              (p.name ?? "") +
              "</b>" +
              (val
                ? '<br/><span style="color:#6b7280">' + val + "</span>"
                : "")
            );
          },
        },
      } as EChartsOption);
    }

    const ro = new ResizeObserver(() => {
      chartInstance.resize();
    });
    ro.observe(chartRef.current);

    return () => {
      ro.disconnect();
      chartInstance.dispose();
    };
  }, [option]);

  return <div ref={chartRef} className="chart-frame" />;
};
