'use client';
import { useMemo, useState } from "react";
import {
  ChevronDown,
  ChevronRight,
  FolderPlus,
  MessageCircle,
  MoreHorizontal,
  Plus,
  Trash2,
} from "lucide-react";
import { ChatTreeNode } from "@/types/chat";

type ChatSessionTreePanelProps = {
  tree: ChatTreeNode[];
  activeNodeId?: string;
  onSelectNode: (nodeId: string) => void;
  onCreateGroup: (parentId?: string) => void;
  onCreateSession: (parentId: string) => void;
  onRenameNode: (nodeId: string, name: string) => void;
  onDeleteNode: (nodeId: string) => void;
  loading: boolean;
};

function isGroup(node: ChatTreeNode) {
  return node.type === "group";
}

export default function ChatSessionTreePanel({
  tree,
  activeNodeId,
  onSelectNode,
  onCreateGroup,
  onCreateSession,
  onRenameNode,
  onDeleteNode,
  loading,
}: ChatSessionTreePanelProps) {
  const [expanded, setExpanded] = useState<Record<string, boolean>>({});

  const handleToggle = (nodeId: string) => {
    setExpanded((prev) => ({ ...prev, [nodeId]: !prev[nodeId] }));
  };

  const root = useMemo(() => tree, [tree]);

  const renderNode = (node: ChatTreeNode) => {
    const isActive = activeNodeId === node.id;
    const hasChildren = !!node.children?.length;
    const open = expanded[node.id] ?? true;

    return (
      <div key={node.id} className="space-y-1">
        <div
          className={`group flex items-center justify-between rounded-lg border border-transparent px-2 py-1.5 text-sm transition ${
            isActive ? "bg-sky-50 text-sky-700 border-sky-100" : "hover:bg-slate-50"
          }`}
        >
          <div className="flex items-center gap-2">
            {hasChildren || isGroup(node) ? (
              <button
                type="button"
                aria-label="toggle"
                className="text-slate-400 hover:text-slate-600"
                onClick={() => handleToggle(node.id)}
              >
                {open ? <ChevronDown size={16} /> : <ChevronRight size={16} />}
              </button>
            ) : (
              <span className="w-4" />
            )}
            <button
              type="button"
              className="flex items-center gap-2 truncate"
              onClick={() => onSelectNode(node.id)}
            >
              {isGroup(node) ? (
                <FolderPlus size={16} className="text-amber-500" />
              ) : (
                <MessageCircle size={16} className="text-sky-500" />
              )}
              <span className="truncate font-medium">{node.name}</span>
            </button>
          </div>
          <div className="flex items-center gap-2 opacity-0 transition group-hover:opacity-100">
            {isGroup(node) && (
              <button
                type="button"
                aria-label="new chat"
                className="text-slate-400 hover:text-slate-700"
                onClick={() => onCreateSession(node.id)}
              >
                <Plus size={14} />
              </button>
            )}
            <button
              type="button"
              aria-label="rename"
              className="text-slate-400 hover:text-slate-700"
              onClick={() => {
                const next = window.prompt("重命名该节点", node.name);
                if (next) onRenameNode(node.id, next);
              }}
            >
              <MoreHorizontal size={14} />
            </button>
            <button
              type="button"
              aria-label="delete"
              className="text-rose-400 hover:text-rose-600"
              onClick={() => onDeleteNode(node.id)}
            >
              <Trash2 size={14} />
            </button>
          </div>
        </div>
        {isGroup(node) && open && node.children?.length ? (
          <div className="space-y-1 pl-5">
            {node.children.map((child) => renderNode(child))}
          </div>
        ) : null}
      </div>
    );
  };

  return (
    <aside className="panel chat-side">
      <div className="panel-header px-4 chat-side-header">
        <div>
          <p className="text-[11px] uppercase tracking-[0.08em] text-slate-400">会话</p>
          <p className="panel-title">树形管理</p>
        </div>
        <button
          type="button"
          className="btn btn-neutral text-sky-700 hover:text-sky-800"
          onClick={() => onCreateGroup()}
        >
          <Plus size={14} />
          新建分组
        </button>
      </div>
      <div className="panel-body space-y-2 overflow-y-auto chat-side-body">
        {loading ? (
          <div className="flex h-20 items-center justify-center text-sm text-slate-400">加载中…</div>
        ) : root.length ? (
          root.map((node) => renderNode(node))
        ) : (
          <div className="empty-state">暂无会话，先创建一个分组或会话吧。</div>
        )}
      </div>
    </aside>
  );
}
