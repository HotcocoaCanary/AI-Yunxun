"use client";

import {useMemo, useState} from "react";
import type {EChartsOption} from "echarts";
import type {ToolInvocation} from "@/types/chat";
import {EChartGraph} from "@/ui/components/assistant_message_card/EChartGraph";

export function ToolCard({tool}: { tool: ToolInvocation }) {
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
            }
            return null;
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
        <div>
            <button type="button" onClick={() => setOpen((prev) => !prev)}>
                Tool {open ? "▾" : "▸"} {tool.name}
            </button>
            {tool.status && <div>{tool.status}</div>}
            {open && (
                <div>
                    <div>
                        <div>args</div>
                        <pre>{JSON.stringify(tool.args ?? {}, null, 2)}</pre>
                    </div>
                    {graphOption ? (
                        <div>
                            <div>
                                <EChartGraph option={graphOption}/>
                            </div>
                        </div>
                    ) : (
                        <div>
                            <div>result</div>
                            <pre>{resultText}</pre>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}
