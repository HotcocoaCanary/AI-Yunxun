"use client";

import { useMemo, useState } from "react";
import type { EChartsOption } from "echarts";
import type { ToolInvocation } from "@/types/chat";
import { EChartGraph } from "@/ui/components/assistant_message_card/EChartGraph";

export function ToolCard({ tool }: { tool: ToolInvocation }) {
  const [open, setOpen] = useState(true);

  const graphOption = useMemo(() => {
    if (!tool.name?.startsWith("echart")) return null;
    if (typeof tool.result === "string") {
      try {
        const parsed = JSON.parse(tool.result);
        if (parsed && typeof parsed === "object") {
          return parsed as EChartsOption;
        }
      } catch {
        return null;
      }
    }
    if (tool.result && typeof tool.result === "object") {
      return tool.result as EChartsOption;
    }
    return null;
  }, [tool.name, tool.result]);

  const resultText =
    typeof tool.result === "string"
      ? tool.result
      : JSON.stringify(tool.result ?? {}, null, 2);

  return (
    <div className="subcard tool-card">
      <button
        type="button"
        className="subcard-toggle"
        aria-expanded={open}
        onClick={() => setOpen((prev) => !prev)}
      >
        <span className="subcard-title">Tool: {tool.name}</span>
        {tool.status && <span className="subcard-status">{tool.status}</span>}
        <span className="subcard-state">{open ? "Hide" : "Show"}</span>
      </button>
      {open && (
        <div className="subcard-body">
          <div className="tool-grid">
            {graphOption ? (
              <div className="tool-section tool-chart">
                <div className="tool-section-title">Chart</div>
                <EChartGraph option={graphOption} />
              </div>
            ) : (
              <div className="tool-section">
                <div className="tool-section-title">Args</div>
                <pre>{JSON.stringify(tool.args ?? {}, null, 2)}</pre>
                <div className="tool-section-title">Result</div>
                <pre>{resultText}</pre>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
