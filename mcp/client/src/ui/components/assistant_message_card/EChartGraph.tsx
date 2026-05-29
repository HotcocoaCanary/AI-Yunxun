"use client";

import { useEffect, useRef } from "react";
import * as echarts from "echarts";
import type { EChartsOption } from "echarts";

interface EChartGraphProps {
  option: EChartsOption;
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
        if (val && typeof val === "object") {
          const t = val as Record<string, unknown>;
          safe.tooltip = {
            confine: true,
            extraCssText: "max-width: 260px; white-space: normal; word-break: break-all; border-radius: 8px;",
            ...t,
          };
        } else {
          safe.tooltip = val;
        }
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
            ...t,
          };
        } else {
          safe.title = val;
        }
        break;
      }

      case "xAxis": {
        const applyAxisLabel = (ax: Record<string, unknown>) => {
          const existingLabel = ax?.axisLabel as Record<string, unknown> | undefined;
          return {
            ...ax,
            axisLabel: {
              fontSize: 11,
              overflow: "truncate",
              width: 100,
              ...existingLabel,
            },
          };
        };
        if (Array.isArray(val)) {
          safe.xAxis = val.map((ax) =>
            applyAxisLabel(typeof ax === "object" ? ax as Record<string, unknown> : {})
          );
        } else if (val && typeof val === "object") {
          safe.xAxis = applyAxisLabel(val as Record<string, unknown>);
        } else {
          safe.xAxis = val;
        }
        break;
      }

      case "yAxis": {
        const applyAxisLabel = (ay: Record<string, unknown>) => {
          const existingLabel = ay?.axisLabel as Record<string, unknown> | undefined;
          return {
            ...ay,
            axisLabel: {
              fontSize: 11,
              overflow: "truncate",
              width: 80,
              ...existingLabel,
            },
          };
        };
        if (Array.isArray(val)) {
          safe.yAxis = val.map((ay) =>
            applyAxisLabel(typeof ay === "object" ? ay as Record<string, unknown> : {})
          );
        } else if (val && typeof val === "object") {
          safe.yAxis = applyAxisLabel(val as Record<string, unknown>);
        } else {
          safe.yAxis = val;
        }
        break;
      }

      case "series": {
        if (Array.isArray(val)) {
          safe.series = (val as Record<string, unknown>[]).map((s) => {
            const existingLabel = s?.label as Record<string, unknown> | undefined;
            const emphasis = (s?.emphasis as Record<string, unknown>) || {
              focus: "series",
            };
            return {
              ...s,
              emphasis: {
                focus: "series",
                ...emphasis,
              },
              label: existingLabel
                ? {
                    overflow: "truncate",
                    width: 80,
                    fontSize: 11,
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

      default:
        safe[key] = val;
    }
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
    chartInstance.setOption(safeOption);

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
