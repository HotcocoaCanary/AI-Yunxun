'use client';
import { McpToolStatus } from "@/types/mcp";

type McpToolsManagerProps = {
  tools: McpToolStatus[];
  loading: boolean;
  onToggleTool: (name: string, enabled: boolean) => Promise<void>;
};

export default function McpToolsManager({
  tools,
  loading,
  onToggleTool,
}: McpToolsManagerProps) {
  const handleToggle = async (tool: McpToolStatus) => {
    await onToggleTool(tool.name, !tool.enabled);
  };

  return (
    <section className="section-card">
      <div className="mb-3">
        <p className="text-xs uppercase tracking-wide text-slate-400">MCP</p>
        <h2 className="text-xl font-semibold text-slate-900">工具管理</h2>
      </div>
      <div className="space-y-3">
        {loading ? (
          <p className="text-sm text-slate-500">加载工具列表...</p>
        ) : tools.length ? (
          tools.map((tool) => (
            <div
              key={tool.name}
              className="flex items-center justify-between rounded-2xl border border-slate-100 bg-slate-50/80 px-4 py-3"
            >
              <div>
                <p className="text-sm font-semibold text-slate-900">{tool.displayName}</p>
                <p className="text-xs text-slate-500">{tool.description}</p>
                {tool.tags?.length ? (
                  <div className="mt-2 flex flex-wrap gap-2 text-[11px] text-slate-500">
                    {tool.tags.map((tag) => (
                      <span
                        key={tag}
                        className="rounded-full bg-white px-2 py-0.5 text-slate-600"
                      >
                        {tag}
                      </span>
                    ))}
                  </div>
                ) : null}
              </div>
              <button
                type="button"
                onClick={() => handleToggle(tool)}
                className={`inline-flex items-center rounded-full px-3 py-1 text-xs font-semibold ${
                  tool.enabled
                    ? "bg-emerald-100 text-emerald-700"
                    : "bg-slate-200 text-slate-600"
                }`}
                disabled={loading}
              >
                {tool.enabled ? "启用中" : "已禁用"}
              </button>
            </div>
          ))
        ) : (
          <p className="text-sm text-slate-500">暂无工具配置。</p>
        )}
      </div>
    </section>
  );
}
