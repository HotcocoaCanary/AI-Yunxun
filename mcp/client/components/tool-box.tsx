"use client";

import { useState } from "react";
import { EChartGraph } from "./echart-graph"; // 确保引入了
export function ToolBox({ tool }: { tool: any }) {
    const [isExpanded, setIsExpanded] = useState(false);

    return (
        <div className="rounded-2xl border border-gray-100 bg-gray-50/50 overflow-hidden transition-all duration-300 hover:border-blue-200">
            <button
                onClick={() => setIsExpanded(!isExpanded)}
                className="w-full flex items-center justify-between p-4 hover:bg-gray-100/50 transition-colors"
            >
                <div className="flex items-center gap-3">
                    <div className={`flex items-center justify-center w-8 h-8 rounded-full ${tool.status === 'done' ? 'bg-green-100 text-green-600' : 'bg-blue-100 text-blue-600'}`}>
                        {tool.status === 'done' ? '✓' : '...'}
                    </div>
                    <div className="text-left">
                        <div className="text-[12px] font-semibold text-gray-700 uppercase tracking-tight">{tool.name}</div>
                        <div className="text-[10px] text-gray-400">Execution {tool.status}</div>
                    </div>
                </div>
                <div className="text-xs text-blue-500 font-medium">
                    {isExpanded ? 'Hide Details' : 'View Graph'}
                </div>
            </button>

            {isExpanded && (
                <div className="p-4 bg-white border-t border-gray-100 animate-in">
                    {tool.ui_type === "echart" ? (
                        <div className="rounded-xl overflow-hidden border border-gray-50 shadow-inner bg-[#fcfcfc]">
                            <EChartGraph config={tool.result} />
                        </div>
                    ) : (
                        <pre className="text-[12px] leading-relaxed p-4 bg-gray-900 text-gray-300 rounded-xl overflow-x-auto">
                            <code>{tool.result}</code>
                        </pre>
                    )}
                </div>
            )}
        </div>
    );
}