export type GraphNode = {
  id: string;
  label: string;
  type: string;
  properties: Record<string, any>;
};

export type GraphEdge = {
  id: string;
  source: string;
  target: string;
  type: string;
  properties: Record<string, any>;
};

export type GraphData = {
  nodes: GraphNode[];
  edges: GraphEdge[];
};
