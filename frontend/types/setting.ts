export type DataStats = {
  mongoRawDocuments: number;
  mongoAnalysisDocuments?: number;
  neo4jNodes: number;
  neo4jRelations: number;
  lastUpdateTime?: string;
};
