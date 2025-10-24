'use client';

import { useState } from 'react';
import { Network, MousePointer } from 'lucide-react';

interface GraphDisplayProps {
  data: any;
}

export default function GraphDisplay({ data }: GraphDisplayProps) {
  const [selectedNode, setSelectedNode] = useState<string | null>(null);

  // 生成示例数据
  const generateSampleData = () => {
    return [
      { id: '1', label: '机器学习', x: 100, y: 100, type: 'concept' },
      { id: '2', label: '深度学习', x: 200, y: 150, type: 'concept' },
      { id: '3', label: '神经网络', x: 300, y: 200, type: 'concept' },
      { id: '4', label: '卷积神经网络', x: 400, y: 100, type: 'concept' },
      { id: '5', label: '循环神经网络', x: 400, y: 300, type: 'concept' },
      { id: '6', label: '自然语言处理', x: 200, y: 300, type: 'application' },
      { id: '7', label: '计算机视觉', x: 300, y: 350, type: 'application' }
    ];
  };

  const nodes = data?.elements?.filter((el: any) => el.data.id && !el.data.source) || generateSampleData();
  const edges = data?.elements?.filter((el: any) => el.data.source) || [
    { source: '1', target: '2', label: '包含' },
    { source: '2', target: '3', label: '基于' },
    { source: '3', target: '4', label: '类型' },
    { source: '3', target: '5', label: '类型' },
    { source: '1', target: '6', label: '应用' },
    { source: '1', target: '7', label: '应用' },
    { source: '4', target: '7', label: '用于' },
    { source: '5', target: '6', label: '用于' }
  ];

  const getNodeColor = (type: string) => {
    switch (type) {
      case 'concept':
        return 'bg-amber-500';
      case 'application':
        return 'bg-blue-500';
      default:
        return 'bg-gray-500';
    }
  };

  return (
    <div className="bg-white rounded-lg border border-gray-200 p-4">
      <div className="mb-4">
        <h3 className="text-lg font-semibold text-gray-900 mb-2">知识图谱</h3>
        <p className="text-sm text-gray-600">点击节点查看详细信息</p>
      </div>
      
      {/* 简化的图谱显示 */}
      <div className="relative w-full h-96 border border-gray-200 rounded-lg overflow-hidden bg-gray-50">
        <svg className="w-full h-full">
          {/* 绘制边 */}
          {edges.map((edge: any, index: number) => {
            const sourceNode = nodes.find((n: any) => n.id === edge.source);
            const targetNode = nodes.find((n: any) => n.id === edge.target);
            if (!sourceNode || !targetNode) return null;
            
            return (
              <g key={index}>
                <line
                  x1={sourceNode.x}
                  y1={sourceNode.y}
                  x2={targetNode.x}
                  y2={targetNode.y}
                  stroke="#9CA3AF"
                  strokeWidth="2"
                  markerEnd="url(#arrowhead)"
                />
                <text
                  x={(sourceNode.x + targetNode.x) / 2}
                  y={(sourceNode.y + targetNode.y) / 2 - 5}
                  textAnchor="middle"
                  className="text-xs fill-gray-600"
                >
                  {edge.label}
                </text>
              </g>
            );
          })}
          
          {/* 绘制节点 */}
          {nodes.map((node: any) => (
            <g key={node.id}>
              <circle
                cx={node.x}
                cy={node.y}
                r="30"
                className={`${getNodeColor(node.type)} cursor-pointer transition-all duration-200 hover:scale-110 ${
                  selectedNode === node.id ? 'ring-4 ring-amber-300' : ''
                }`}
                onClick={() => setSelectedNode(selectedNode === node.id ? null : node.id)}
              />
              <text
                x={node.x}
                y={node.y + 5}
                textAnchor="middle"
                className="text-xs font-medium fill-white pointer-events-none"
              >
                {node.label}
              </text>
            </g>
          ))}
          
          {/* 箭头标记 */}
          <defs>
            <marker
              id="arrowhead"
              markerWidth="10"
              markerHeight="7"
              refX="9"
              refY="3.5"
              orient="auto"
            >
              <polygon
                points="0 0, 10 3.5, 0 7"
                fill="#9CA3AF"
              />
            </marker>
          </defs>
        </svg>
        
        {/* 图例 */}
        <div className="absolute top-4 right-4 bg-white rounded-lg shadow-sm p-3 border border-gray-200">
          <h4 className="text-sm font-medium text-gray-900 mb-2">图例</h4>
          <div className="space-y-1">
            <div className="flex items-center space-x-2">
              <div className="w-3 h-3 bg-amber-500 rounded-full"></div>
              <span className="text-xs text-gray-600">概念</span>
            </div>
            <div className="flex items-center space-x-2">
              <div className="w-3 h-3 bg-blue-500 rounded-full"></div>
              <span className="text-xs text-gray-600">应用</span>
            </div>
          </div>
        </div>
      </div>
      
      {/* 节点详情 */}
      {selectedNode && (
        <div className="mt-4 p-4 bg-amber-50 rounded-lg border border-amber-200">
          <h4 className="font-medium text-gray-900 mb-2">节点详情</h4>
          <p className="text-sm text-gray-600">
            选中节点: {nodes.find((n: any) => n.id === selectedNode)?.label}
          </p>
        </div>
      )}
    </div>
  );
}
