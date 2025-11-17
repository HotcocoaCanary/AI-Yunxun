'use client';

import ReactECharts from 'echarts-for-react';

interface GraphRendererProps {
  graph?: {
    nodes?: { id: string | number; labels?: string[]; properties?: Record<string, unknown> }[];
    links?: { id: string | number; source: string | number; target: string | number; type?: string; properties?: Record<string, unknown> }[];
  } | null;
}

export default function GraphRenderer({ graph }: GraphRendererProps) {
  if (!graph || !graph.nodes || graph.nodes.length === 0) {
    return (
      <div className="graph-container flex items-center justify-center text-sm text-gray-400">
        暂无图谱数据
      </div>
    );
  }

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: (params: any) => {
        if (params.dataType === 'edge') {
          return `${params.data.type || '关系'}<br/>${params.data.source} → ${params.data.target}`;
        }
        const labels = params.data.labels?.join(', ');
        return `${labels || '节点'}<br/>${params.data.name || params.data.id}`;
      }
    },
    series: [
      {
        type: 'graph',
        layout: 'force',
        roam: true,
        label: {
          show: true,
          color: '#111827',
          fontWeight: 500
        },
        force: {
          repulsion: 260,
          edgeLength: 120
        },
        data: graph.nodes.map((node) => ({
          id: String(node.id),
          name: node.properties?.name || node.properties?.title || `节点 ${node.id}`,
          value: node.properties,
          labels: node.labels,
          symbolSize: 40 + ((node.properties?.weight as number) || 0) * 4,
        })),
        links: (graph.links || []).map((edge) => ({
          id: edge.id,
          source: String(edge.source),
          target: String(edge.target),
          type: edge.type,
        }))
      }
    ]
  };

  return (
    <div className="graph-container">
      <ReactECharts option={option} style={{ height: '100%', width: '100%' }} />
    </div>
  );
}
