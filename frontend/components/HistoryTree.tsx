'use client';

import { FolderTree, MessageSquare } from 'lucide-react';
import clsx from 'clsx';

export interface HistoryNode {
  id: string;
  label: string;
  type: 'folder' | 'conversation';
  updatedAt?: string;
  children?: HistoryNode[];
}

interface HistoryTreeProps {
  data: HistoryNode[];
  activeId?: string;
  onSelect: (conversationId: string) => void;
}

export default function HistoryTree({ data, activeId, onSelect }: HistoryTreeProps) {
  return (
    <div className="space-y-4">
      {data.map((node) => (
        <TreeNode key={node.id} node={node} depth={0} activeId={activeId} onSelect={onSelect} />
      ))}
    </div>
  );
}

interface TreeNodeProps {
  node: HistoryNode;
  depth: number;
  activeId?: string;
  onSelect: (conversationId: string) => void;
}

function TreeNode({ node, depth, activeId, onSelect }: TreeNodeProps) {
  const paddingLeft = depth * 12;
  if (node.type === 'folder') {
    return (
      <div>
        <div className="flex items-center text-xs uppercase tracking-wide text-gray-500 mb-2" style={{ paddingLeft }}>
          <FolderTree className="w-3 h-3 mr-2" />
          {node.label}
        </div>
        <div className="space-y-1">
          {node.children?.map((child) => (
            <TreeNode
              key={child.id}
              node={child}
              depth={depth + 1}
              activeId={activeId}
              onSelect={onSelect}
            />
          ))}
        </div>
      </div>
    );
  }

  return (
    <button
      type="button"
      onClick={() => onSelect(node.id)}
      className={clsx(
        'w-full flex items-center justify-between rounded-lg px-3 py-2 text-left transition-colors',
        activeId === node.id ? 'bg-amber-100 text-amber-700' : 'hover:bg-gray-100 text-gray-700'
      )}
      style={{ paddingLeft }}
    >
      <div className="flex items-center space-x-2">
        <MessageSquare className="w-4 h-4" />
        <span className="truncate">{node.label}</span>
      </div>
      {node.updatedAt && (
        <span className="text-xs text-gray-400">{node.updatedAt}</span>
      )}
    </button>
  );
}
