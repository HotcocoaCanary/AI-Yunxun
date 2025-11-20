'use client';

import ToolTogglePanel, { ToolInfo } from '@/components/ToolTogglePanel';
import { ArrowRight } from 'lucide-react';

interface PersonalDrawerProps {
  open: boolean;
  onClose: () => void;
  availableTools: ToolInfo[];
  enabledTools: string[];
  onToggleTool: (toolId: string, next: boolean) => void;
  crawlerSources: string[];
  newSource: string;
  onNewSourceChange: (value: string) => void;
  onAddSource: () => void;
}

export default function PersonalDrawer({
  open,
  onClose,
  availableTools,
  enabledTools,
  onToggleTool,
  crawlerSources,
  newSource,
  onNewSourceChange,
  onAddSource,
}: PersonalDrawerProps) {
  if (!open) return null;

  return (
    <div className="personal-drawer">
      <div className="personal-drawer__header">
        <div>
          <p className="personal-drawer__title">个人中心</p>
          <p className="personal-drawer__subtitle">管理 MCP 工具、偏好与数据策略</p>
        </div>
        <button type="button" className="drawer-close" onClick={onClose}>
          <ArrowRight className="h-4 w-4 rotate-180" />
        </button>
      </div>
      <div className="personal-drawer__grid">
        <section className="personal-drawer__section">
          <p className="personal-drawer__section-title">MCP 工具</p>
          <ToolTogglePanel
            tools={availableTools}
            enabledTools={enabledTools}
            onToggle={onToggleTool}
          />
        </section>
        <section className="personal-drawer__section">
          <p className="personal-drawer__section-title">数据源管理</p>
          <div className="source-panel">
            <div className="source-list">
              {crawlerSources.map((source) => (
                <div key={source} className="source-list__item">
                  <span>{source}</span>
                </div>
              ))}
            </div>
            <div className="source-form">
              <input
                value={newSource}
                onChange={(event) => onNewSourceChange(event.target.value)}
                placeholder="新增站点或 API"
                className="source-input"
              />
              <button type="button" className="source-button" onClick={onAddSource}>
                添加
              </button>
            </div>
          </div>
        </section>
      </div>
    </div>
  );
}
