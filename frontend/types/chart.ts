export type ChartType = "bar" | "line" | "pie" | "table";

export type ChartSpec = {
  id: string;
  title: string;
  type: ChartType;
  xField?: string;
  yField?: string;
  seriesField?: string;
  data: Array<Record<string, any>>;
  extraConfig?: Record<string, any>;
};
