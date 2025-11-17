'use client';

import ReactECharts from 'echarts-for-react';

interface ChartGalleryProps {
  charts: {
    chartType: string;
    title: string;
    options: Record<string, any>;
  }[];
}

export default function ChartGallery({ charts }: ChartGalleryProps) {
  if (!charts || charts.length === 0) {
    return null;
  }

  return (
    <div className="grid grid-cols-1 gap-4">
      {charts.map((chart, index) => (
        <div key={`${chart.title}-${index}`} className="chart-container">
          <div className="flex items-center justify-between mb-2">
            <p className="text-sm font-medium text-gray-900">{chart.title}</p>
            <span className="text-xs text-gray-400 uppercase">{chart.chartType}</span>
          </div>
          <ReactECharts option={chart.options} style={{ height: 280 }} />
        </div>
      ))}
    </div>
  );
}
