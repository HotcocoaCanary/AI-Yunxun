/**
 * Types that mirror the backend MCP tool payloads.
 * These are intentionally minimal for now and can be
 * expanded alongside backend evolution.
 */

export type ChartResponse = {
  schemaVersion: string;
  chartType: string;
  engine: string;
  title?: string;
  description?: string;
  chartSpec: Record<string, unknown>;
  data?: Array<Record<string, unknown>>;
  insightSummary?: string;
  insightBullets?: string[];
};

