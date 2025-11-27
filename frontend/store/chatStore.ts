import { create } from "zustand";
import { ChatMessage, ChatTreeNode } from "@/types/chat";

interface ChatState {
  tree: ChatTreeNode[];
  activeNodeId?: string;
  messagesBySession: Record<string, ChatMessage[]>;
  loading: boolean;
  setTree: (tree: ChatTreeNode[]) => void;
  setActiveNodeId: (id: string) => void;
  setMessagesForSession: (sessionId: string, messages: ChatMessage[]) => void;
  appendMessage: (sessionId: string, message: ChatMessage) => void;
  updateMessage: (
    sessionId: string,
    messageId: string,
    updater: (message: ChatMessage) => ChatMessage
  ) => void;
  createGroup: (parentId?: string) => void;
  createSession: (parentId: string) => void;
  renameNode: (nodeId: string, name: string) => void;
  deleteNode: (nodeId: string) => void;
}

function addChild(
  nodes: ChatTreeNode[],
  parentId: string | undefined,
  node: ChatTreeNode
): ChatTreeNode[] {
  if (!parentId) {
    return [...nodes, node];
  }
  return nodes.map((item) => {
    if (item.id === parentId) {
      const children = item.children ? [...item.children, node] : [node];
      return { ...item, children };
    }
    if (item.children?.length) {
      return { ...item, children: addChild(item.children, parentId, node) };
    }
    return item;
  });
}

function updateNode(
  nodes: ChatTreeNode[],
  nodeId: string,
  updater: (node: ChatTreeNode) => ChatTreeNode
): ChatTreeNode[] {
  return nodes.map((item) => {
    if (item.id === nodeId) {
      return updater(item);
    }
    if (item.children?.length) {
      return { ...item, children: updateNode(item.children, nodeId, updater) };
    }
    return item;
  });
}

function removeNode(nodes: ChatTreeNode[], nodeId: string): ChatTreeNode[] {
  const result: ChatTreeNode[] = [];
  for (const node of nodes) {
    if (node.id === nodeId) continue;
    if (node.children?.length) {
      result.push({ ...node, children: removeNode(node.children, nodeId) });
    } else {
      result.push(node);
    }
  }
  return result;
}

export const useChatStore = create<ChatState>((set, get) => ({
  tree: [],
  activeNodeId: undefined,
  messagesBySession: {},
  loading: false,
  setTree: (tree) => set({ tree }),
  setActiveNodeId: (id) => set({ activeNodeId: id }),
  setMessagesForSession: (sessionId, messages) =>
    set((state) => ({
      messagesBySession: { ...state.messagesBySession, [sessionId]: messages },
    })),
  appendMessage: (sessionId, message) =>
    set((state) => {
      const prev = state.messagesBySession[sessionId] || [];
      return {
        messagesBySession: {
          ...state.messagesBySession,
          [sessionId]: [...prev, message],
        },
      };
    }),
  updateMessage: (sessionId, messageId, updater) =>
    set((state) => {
      const prev = state.messagesBySession[sessionId] || [];
      const next = prev.map((msg) =>
        msg.id === messageId ? updater(msg) : msg
      );
      return { messagesBySession: { ...state.messagesBySession, [sessionId]: next } };
    }),
  createGroup: (parentId) =>
    set((state) => {
      const id = `group-${Date.now()}`;
      const next: ChatTreeNode = {
        id,
        type: "group",
        name: "新建分组",
        parentId: parentId ?? null,
        children: [],
      };
      return { tree: addChild(state.tree, parentId, next) };
    }),
  createSession: (parentId) =>
    set((state) => {
      const id = `session-${Date.now()}`;
      const next: ChatTreeNode = {
        id,
        type: "session",
        name: "新建会话",
        parentId,
      };
      return { tree: addChild(state.tree, parentId, next) };
    }),
  renameNode: (nodeId, name) =>
    set((state) => ({ tree: updateNode(state.tree, nodeId, (n) => ({ ...n, name })) })),
  deleteNode: (nodeId) =>
    set((state) => ({
      tree: removeNode(state.tree, nodeId),
      messagesBySession: Object.fromEntries(
        Object.entries(state.messagesBySession).filter(([key]) => key !== nodeId)
      ),
    })),
}));
