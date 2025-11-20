'use client';

import clsx from 'clsx';
import { Menu, PanelLeft, Sparkles, Users } from 'lucide-react';

import HistoryTree, { HistoryNode } from '@/components/HistoryTree';

interface SidebarNavProps {
  sidebarOpen: boolean;
  loadingConversations: boolean;
  history: HistoryNode[];
  selectedConversation?: string;
  onSelectConversation: (conversationId: string) => void;
  onToggleSidebar: () => void;
}

export default function SidebarNav({
  sidebarOpen,
  loadingConversations,
  history,
  selectedConversation,
  onSelectConversation,
  onToggleSidebar,
}: SidebarNavProps) {
  return (
    <aside
      className={clsx('workspace-sidebar', {
        'workspace-sidebar--collapsed': !sidebarOpen,
      })}
    >
      <div className="sidebar-shell">
        <header className="sidebar-header">
          <div className="sidebar-brand">
            <Sparkles className="h-5 w-5 text-amber-500" />
            {sidebarOpen && (
              <div>
                <p className="sidebar-brand__title">AI-Yunxun</p>
                <p className="sidebar-brand__subtitle">研究助手</p>
              </div>
            )}
          </div>
          <button
            type="button"
            aria-label="展开或收起侧边栏"
            className="sidebar-toggle"
            onClick={onToggleSidebar}
          >
            <PanelLeft className="h-4 w-4" />
          </button>
        </header>

        <div className="sidebar-content">
          {sidebarOpen ? (
            loadingConversations ? (
              <div className="sidebar-placeholder">
                <div className="sidebar-placeholder__icon" />
                <p>正在加载会话...</p>
              </div>
            ) : (
              <HistoryTree
                data={history}
                activeId={selectedConversation}
                onSelect={onSelectConversation}
              />
            )
          ) : (
            <div className="sidebar-collapsed-icons">
              <Menu className="h-5 w-5" />
              <Users className="h-5 w-5" />
            </div>
          )}
        </div>
      </div>
    </aside>
  );
}
