export type ChatNodeType = "group" | "session";

export interface ChatTreeNode {
  id: string;
  type: ChatNodeType;
  name: string;
  parentId?: string | null;
  children?: ChatTreeNode[];
  // optional display fields
  isExpanded?: boolean;
  lastMessageAt?: string;
}

export interface ChatMessage {
  id: string;
  role: "user" | "assistant" | "system";
  content: string;
  createdAt: string;
  graph?: import("./graph").GraphData;
  charts?: import("./chart").ChartSpec[];
}

export interface MemoryScopePayload {
  sessionId: string;
  memorySessionIds: string[];
}
