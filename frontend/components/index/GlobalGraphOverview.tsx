'use client';
import { useEffect, useState } from "react";
import { RefreshCw } from "lucide-react";
import { GraphApi } from "@/apis/graphApi";
import { GraphData } from "@/types/graph";
import GraphView from "../graph/GraphView";

export default function GlobalGraphOverview() {
  const [data, setData] = useState<GraphData | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadData = async () => {
    setLoading(true);
    setError(null);
    try {
      const graph = await GraphApi.getGlobalOverview();
      setData(graph);
    } catch (err) {
      setError("加载图谱预览失败，请稍后再试");
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  return (
    <section className="section-card graph-card">
      <div className="section-head">
        <div>
          <p className="chip">Global Graph</p>
          <h2 className="section-title">全局知识图谱预览</h2>
        </div>
        <button type="button" className="btn btn-neutral" onClick={loadData}>
          <RefreshCw size={15} className={loading ? "animate-spin" : ""} />
          重新拉取
        </button>
      </div>
      {loading && (
        <div className="flex h-72 items-center justify-center text-sm text-slate-500">
          正在加载图谱…
        </div>
      )}
      {error && !loading && (
        <div className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-700">
          {error}
        </div>
      )}
      {!loading && data ? <GraphView data={data} height={360} /> : null}
    </section>
  );
}
