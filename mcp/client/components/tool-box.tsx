"use client";

import { useState } from "react";
import { EChartGraph } from "./echart-graph"; // 确保引入了

export function ToolBox({ tool }: { tool: any }) {
    const [isExpanded, setIsExpanded] = useState(false);

    return (
        <div className="border rounded-lg bg-gray-50 overflow-hidden my-2">
            {/* 头部点击区域 */}
            <div
                className="flex items-center justify-between p-2 cursor-pointer bg-gray-50 hover:bg-gray-100 transition-colors"
                onClick={() => setIsExpanded(!isExpanded)}
            >
                <div className="flex items-center gap-2">
                    <div className={`w-2 h-2 rounded-full ${tool.status === 'done' ? 'bg-green-500' : 'bg-blue-400 animate-pulse'}`} />
                    <span className="text-xs font-mono text-gray-700">{tool.name}</span>
                </div>
                <span className="text-[10px] text-gray-400">{isExpanded ? '收起' : '查看结果'}</span>
            </div>

            {/* 展开内容 */}
            {isExpanded && (
                <div className="p-3 border-t bg-white">
                    {tool.status === 'done' && (
                        <div className="space-y-2">
                            {/* 💡 核心：在这里使用 EChartGraph */}
                            {tool.ui_type === "echart" ? (
                                <div className="mt-2">
                                    <EChartGraph config={tool.result} />
                                </div>
                            ) : (
                                <pre className="text-[11px] bg-gray-50 p-2 rounded overflow-x-auto text-gray-600">
                                    {tool.result}
                                </pre>
                            )}
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}