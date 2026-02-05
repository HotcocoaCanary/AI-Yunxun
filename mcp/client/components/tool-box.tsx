"use client";

import { useState } from "react";
import { EChartGraph } from "./echart-graph";
import type { ToolInvocation } from "@/types/chat";
import type { EChartsOption } from "echarts";
export function ToolBox({ tool }: { tool: ToolInvocation }) {
    const [isExpanded, setIsExpanded] = useState(false);
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
        <div className="rounded-2xl border border-gray-100 bg-gray-50/70 overflow-hidden transition-all duration-300 hover:border-blue-200">
            <button
                onClick={() => setIsExpanded(!isExpanded)}
                className="w-full flex items-center justify-between p-4 hover:bg-gray-100/60 transition-colors"
            >
                <div className="flex items-center gap-3">
                    <div className={`flex items-center justify-center w-8 h-8 rounded-full ${tool.status === 'done' ? 'bg-green-100 text-green-600 ring-1 ring-green-200' : 'bg-blue-100 text-blue-600 ring-1 ring-blue-200'}`}>
                        {tool.status === 'done' ? '✓' : '...'}
                    </div>
                    <div className="text-left">
                        <div className="text-[12px] font-semibold text-gray-700 tracking-tight">{tool.name}</div>
                        <div className="text-[10px] text-gray-400">执行状态：{tool.status}</div>
                    </div>
                </div>
                <div className="text-xs text-blue-500 font-medium">
                    {isExpanded ? '收起详情' : (isGraph ? '查看图表' : '查看详情')}
                </div>
            </button>

            {isExpanded && (
                <div className="p-4 bg-white border-t border-gray-100 animate-in">
                    {isGraph && graphOption ? (
                        <div className="rounded-xl overflow-hidden border border-gray-50 shadow-inner bg-[#fcfcfc]">
                            <EChartGraph option={graphOption} />
                        </div>
                    ) : (
                        <pre className="text-[12px] leading-relaxed p-4 bg-gray-900 text-gray-300 rounded-xl overflow-x-auto whitespace-pre-wrap">
                            <code>{tool.result}</code>
                        </pre>
                    )}
                </div>
            )}
        </div>
    );
}
