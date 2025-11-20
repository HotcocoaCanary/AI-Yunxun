import type { PlanStep } from '@/components/PlanTimeline';

export interface AgentChartPayload {
  chartType: string;
  title: string;
  options: Record<string, unknown>;
}

export interface AgentGraphNode {
  id: string | number;
  labels?: string[];
  properties?: Record<string, unknown>;
}

export interface AgentGraphEdge {
  id: string | number;
  source: string | number;
  target: string | number;
  type?: string;
  properties?: Record<string, unknown>;
}

export interface AgentGraphPayload {
  nodes?: AgentGraphNode[];
  links?: AgentGraphEdge[];
}

export interface AgentDocumentSnippet {
  documentId: string;
  title: string;
  summary: string;
  source: string;
  url: string;
}

export interface ConversationMessage {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: string;
  plan?: PlanStep[];
  charts?: AgentChartPayload[];
  graph?: AgentGraphPayload | null;
  documents?: AgentDocumentSnippet[];
}

export interface ConversationSummary {
  id: string;
  title: string;
  updatedAt?: string;
}

export interface AgentMessageDto {
  role: 'user' | 'assistant';
  content: string;
  timestamp?: string;
}

export interface ConversationDetail {
  id: string;
  title: string;
  history: AgentMessageDto[];
  enabledTools: string[];
}

export interface AgentChatResponse {
  answer: string;
  plan?: PlanStep[];
  charts?: AgentChartPayload[];
  graph?: AgentGraphPayload | null;
  documents?: AgentDocumentSnippet[];
  conversationId: string;
}
