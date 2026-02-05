"use client";

import { EChartGraph } from "./echart-graph";
import type { ToolInvocation } from "@/types/chat";
import type { EChartsOption } from "echarts";

export function ToolBox({ tool }: { tool: ToolInvocation }) {
  const isGraph = tool.name?.startsWith("echart");
  let graphOption: EChartsOption | null = null;
  if (isGraph && typeof tool.result === "string") {
    try {
      const parsed = JSON.parse(tool.result);
      if (parsed && typeof parsed === "object") {
        graphOption = parsed;
      }
    } catch {}
  } else if (isGraph && tool.result && typeof tool.result === "object") {
    graphOption = tool.result as EChartsOption;
  }

  return (
    <div>
      <div>{tool.name}</div>
      {tool.status && <div>{tool.status}</div>}
      {isGraph && graphOption ? (
        <EChartGraph option={graphOption} />
      ) : (
        <pre>{typeof tool.result === "string" ? tool.result : JSON.stringify(tool.result)}</pre>
      )}
    </div>
  );
}
