'use client';
import { GraphData } from "@/types/graph";
import GraphView from "./GraphView";

type GraphCardProps = {
  title?: string;
  data: GraphData;
  onOpenFullView?: () => void;
};

export default function GraphCard({
  title = "关联子图",
  data,
  onOpenFullView,
}: GraphCardProps) {
  return (
    <div className="rounded-2xl border border-slate-100 bg-white/90 p-4 shadow-sm">
      <div className="mb-3 flex items-center justify-between">
        <div>
          <p className="text-xs uppercase tracking-wide text-slate-400">Graph</p>
          <h3 className="text-lg font-semibold text-slate-900">{title}</h3>
        </div>
        {onOpenFullView && (
          <button
            type="button"
            className="text-sm font-medium text-sky-600 hover:text-sky-700"
            onClick={onOpenFullView}
          >
            全屏
          </button>
        )}
      </div>
      <GraphView data={data} height={260} />
    </div>
  );
}
