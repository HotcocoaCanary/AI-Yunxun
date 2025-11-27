import { ChartSpec } from "@/types/chart";
import ChartCard from "./ChartCard";

type ChartsPanelProps = {
  charts: ChartSpec[];
};

export default function ChartsPanel({ charts }: ChartsPanelProps) {
  if (!charts?.length) return null;
  return (
    <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
      {charts.map((chart) => (
        <ChartCard key={chart.id} spec={chart} />
      ))}
    </div>
  );
}
