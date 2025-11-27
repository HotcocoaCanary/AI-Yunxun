'use client';
import { RefreshCcw } from "lucide-react";
import { DataStats } from "@/types/setting";

type DataStatsPanelProps = {
  stats: DataStats | null;
  loading: boolean;
  onRefresh: () => void;
};

const STAT_FIELDS: { key: keyof DataStats; label: string }[] = [
  { key: "mongoRawDocuments", label: "Mongo 原始文档" },
  { key: "mongoAnalysisDocuments", label: "Mongo 结构化文档" },
  { key: "neo4jNodes", label: "Neo4j 节点" },
  { key: "neo4jRelations", label: "Neo4j 关系" },
];

export default function DataStatsPanel({ stats, loading, onRefresh }: DataStatsPanelProps) {
  return (
    <section className="section-card">
      <div className="mb-4 flex items-center justify-between">
        <div>
          <p className="text-xs uppercase tracking-wide text-slate-400">Data</p>
          <h2 className="text-xl font-semibold text-slate-900">数据统计</h2>
          <p className="text-xs text-slate-500">
            {stats?.lastUpdateTime ? `上次更新：${stats.lastUpdateTime}` : "实时统计"}
          </p>
        </div>
        <button
          type="button"
          className="btn btn-neutral"
          onClick={onRefresh}
          disabled={loading}
        >
          <RefreshCcw size={14} className={loading ? "animate-spin" : ""} />
          刷新
        </button>
      </div>
      <div className="grid grid-cols-2 gap-3 md:grid-cols-4">
        {STAT_FIELDS.map((field) => (
          <div
            key={field.key}
            className="rounded-2xl border border-slate-100 bg-slate-50/60 p-4 text-sm"
          >
            <p className="text-slate-500">{field.label}</p>
            <p className="mt-2 text-2xl font-semibold text-slate-900">
              {stats ? stats[field.key] ?? "-" : "-"}
            </p>
          </div>
        ))}
      </div>
    </section>
  );
}
