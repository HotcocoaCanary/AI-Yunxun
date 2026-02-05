"use client";

import { useState } from "react";

export function ThoughtBox({ content }: { content: string }) {
    const [isExpanded, setIsExpanded] = useState(true);
    if (!content) return null;

    return (
        <div className="rounded-2xl border border-gray-100 bg-gray-50/70 overflow-hidden transition-all duration-300 hover:border-blue-200">
            <button
                type="button"
                onClick={() => setIsExpanded(!isExpanded)}
                className="w-full flex items-center justify-between p-4 hover:bg-gray-100/60 transition-colors"
            >
                <div className="flex items-center gap-3">
                    <div className={`flex items-center justify-center w-8 h-8 rounded-full ${isExpanded ? "bg-blue-100 text-blue-600 ring-1 ring-blue-200" : "bg-gray-100 text-gray-500 ring-1 ring-gray-200"}`}>
                        T
                    </div>
                    <div className="text-left">
                        <div className="text-[12px] font-semibold text-gray-700 uppercase tracking-tight">\u601D\u7EF4\u94FE</div>
                        <div className="text-[10px] text-gray-400">Reasoning</div>
                    </div>
                </div>
                <div className="text-xs text-blue-500 font-medium">
                    {isExpanded ? "收起详情" : "展开详情"}
                </div>
            </button>

            {isExpanded && (
                <div className="p-4 bg-white border-t border-gray-100 animate-in">
                    <pre className="text-[12px] leading-relaxed p-4 bg-gray-900 text-gray-300 rounded-xl overflow-x-auto whitespace-pre-wrap">
                        <code>{content}</code>
                    </pre>
                </div>
            )}
        </div>
    );
}
