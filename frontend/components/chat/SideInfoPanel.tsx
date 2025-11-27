import { MemoryScopePayload } from "@/types/chat";
import { McpToolStatus } from "@/types/mcp";

type SideInfoPanelProps = {
  memoryScope?: MemoryScopePayload;
  tools?: McpToolStatus[];
};

export default function SideInfoPanel({ memoryScope, tools = [] }: SideInfoPanelProps) {
  return (
    <aside className="info-panel section-card w-full max-w-sm space-y-4">
      <div className="panel">
        <div className="panel-header">
          <p className="text-xs uppercase tracking-wide text-slate-400">记忆范围</p>
        </div>
        <div className="panel-body">
          {memoryScope ? (
            <div className="space-y-1 text-sm text-slate-700">
              <p>
                当前会话：<span className="font-semibold">{memoryScope.sessionId}</span>
              </p>
              <p>
                共享记忆会话数：
                <span className="font-semibold">{memoryScope.memorySessionIds.length}</span>
              </p>
            </div>
          ) : (
            <p className="text-sm text-slate-500">选择一个会话查看上下文范围。</p>
          )}
        </div>
      </div>

      <div className="panel">
        <div className="panel-header">
          <p className="text-xs uppercase tracking-wide text-slate-400">MCP 工具</p>
          <span className="rounded-full bg-slate-100 px-2 py-0.5 text-xs text-slate-600">
            {tools.length} 个
          </span>
        </div>
        <div className="panel-body space-y-2">
          {tools.length ? (
            tools.map((tool) => (
              <div
                key={tool.name}
                className="flex items-center justify-between rounded-xl border border-slate-100 bg-slate-50 px-3 py-2 text-sm"
              >
                <div>
                  <p className="font-semibold text-slate-800">{tool.displayName}</p>
                  <p className="text-xs text-slate-500">{tool.description}</p>
                </div>
                <span
                  className={`rounded-full px-2 py-0.5 text-xs font-semibold ${
                    tool.enabled ? "bg-emerald-100 text-emerald-700" : "bg-slate-100 text-slate-500"
                  }`}
                >
                  {tool.enabled ? "启用" : "停用"}
                </span>
              </div>
            ))
          ) : (
            <p className="text-sm text-slate-500">暂无工具配置。</p>
          )}
        </div>
      </div>
    </aside>
  );
}
