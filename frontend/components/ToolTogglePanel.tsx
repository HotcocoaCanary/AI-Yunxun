'use client';

import clsx from 'clsx';

export interface ToolInfo {
  id: string;
  name: string;
  description: string;
}

interface ToolTogglePanelProps {
  tools: ToolInfo[];
  enabledTools: string[];
  onToggle: (toolId: string, next: boolean) => void;
}

export default function ToolTogglePanel({ tools, enabledTools, onToggle }: ToolTogglePanelProps) {
  return (
    <div className="rounded-xl border border-gray-200 p-4 bg-white">
      <div className="flex items-center justify-between mb-3">
        <div>
          <p className="text-sm font-semibold text-gray-900">MCP 工具</p>
          <p className="text-xs text-gray-500">启用或禁用可用工具</p>
        </div>
        <span className="text-xs text-amber-600">{enabledTools.length} / {tools.length}</span>
      </div>
      <div className="space-y-3">
        {tools.map((tool) => {
          const enabled = enabledTools.includes(tool.id);
          return (
            <div key={tool.id} className="flex items-start justify-between">
              <div className="mr-3">
                <p className="text-sm font-medium text-gray-900">{tool.name}</p>
                <p className="text-xs text-gray-500">{tool.description}</p>
              </div>
              <button
                type="button"
                onClick={() => onToggle(tool.id, !enabled)}
                className={clsx(
                  'relative inline-flex h-6 w-11 items-center rounded-full transition-colors focus:outline-none focus:ring-2 focus:ring-amber-500 focus:ring-offset-2',
                  enabled ? 'bg-amber-500' : 'bg-gray-200'
                )}
              >
                <span
                  className={clsx(
                    'inline-block h-4 w-4 transform rounded-full bg-white transition-transform',
                    enabled ? 'translate-x-5' : 'translate-x-1'
                  )}
                />
              </button>
            </div>
          );
        })}
      </div>
    </div>
  );
}
