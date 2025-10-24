'use client';

import { useState, useEffect } from 'react';
import { Search, Filter, Download, Upload, RefreshCw } from 'lucide-react';
import GraphDisplay from './GraphDisplay';
import ChartDisplay from './ChartDisplay';

export default function KnowledgeGraph() {
  const [searchQuery, setSearchQuery] = useState('');
  const [graphData, setGraphData] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [filters, setFilters] = useState({
    nodeType: 'all',
    relationshipType: 'all',
    timeRange: 'all'
  });

  const handleSearch = async () => {
    if (!searchQuery.trim()) return;

    setIsLoading(true);
    try {
      // 调用后端API进行图谱查询
      const response = await fetch('/api/intelligent/query', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ 
          query: searchQuery,
          type: 'graph'
        }),
      });

      const data = await response.json();
      
      if (data.success) {
        setGraphData(data.data.visualizationData);
      } else {
        console.error('查询失败:', data.message);
      }
    } catch (error) {
      console.error('查询失败:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleExport = () => {
    if (!graphData) return;
    
    const dataStr = JSON.stringify(graphData, null, 2);
    const dataBlob = new Blob([dataStr], { type: 'application/json' });
    const url = URL.createObjectURL(dataBlob);
    const link = document.createElement('a');
    link.href = url;
    link.download = 'knowledge-graph.json';
    link.click();
    URL.revokeObjectURL(url);
  };

  const handleImport = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = (e) => {
      try {
        const data = JSON.parse(e.target?.result as string);
        setGraphData(data);
      } catch (error) {
        console.error('文件解析失败:', error);
        alert('文件格式不正确');
      }
    };
    reader.readAsText(file);
  };

  return (
    <div className="h-full flex flex-col bg-white">
      {/* 头部工具栏 */}
      <div className="border-b border-gray-200 p-4">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-xl font-semibold text-gray-900">知识图谱可视化</h2>
          <div className="flex space-x-2">
            <button
              onClick={handleExport}
              disabled={!graphData}
              className="btn-outline flex items-center space-x-2 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <Download className="w-4 h-4" />
              <span>导出</span>
            </button>
            <label className="btn-outline flex items-center space-x-2 cursor-pointer">
              <Upload className="w-4 h-4" />
              <span>导入</span>
              <input
                type="file"
                accept=".json"
                onChange={handleImport}
                className="hidden"
              />
            </label>
          </div>
        </div>

        {/* 搜索和筛选 */}
        <div className="flex flex-col md:flex-row gap-4">
          <div className="flex-1">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
                placeholder="搜索实体、关系或输入自然语言查询..."
                className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-amber-500 focus:border-transparent"
              />
            </div>
          </div>
          <button
            onClick={handleSearch}
            disabled={isLoading || !searchQuery.trim()}
            className="btn-primary flex items-center space-x-2 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {isLoading ? (
              <RefreshCw className="w-4 h-4 animate-spin" />
            ) : (
              <Search className="w-4 h-4" />
            )}
            <span>搜索</span>
          </button>
        </div>

        {/* 筛选器 */}
        <div className="flex flex-wrap gap-4 mt-4">
          <div className="flex items-center space-x-2">
            <label className="text-sm font-medium text-gray-700">节点类型:</label>
            <select
              value={filters.nodeType}
              onChange={(e) => setFilters(prev => ({ ...prev, nodeType: e.target.value }))}
              className="px-3 py-1 border border-gray-300 rounded-md text-sm focus:ring-2 focus:ring-amber-500 focus:border-transparent"
            >
              <option value="all">全部</option>
              <option value="paper">论文</option>
              <option value="author">作者</option>
              <option value="institution">机构</option>
              <option value="keyword">关键词</option>
            </select>
          </div>
          <div className="flex items-center space-x-2">
            <label className="text-sm font-medium text-gray-700">关系类型:</label>
            <select
              value={filters.relationshipType}
              onChange={(e) => setFilters(prev => ({ ...prev, relationshipType: e.target.value }))}
              className="px-3 py-1 border border-gray-300 rounded-md text-sm focus:ring-2 focus:ring-amber-500 focus:border-transparent"
            >
              <option value="all">全部</option>
              <option value="cites">引用</option>
              <option value="coauthors">合作</option>
              <option value="belongs_to">属于</option>
              <option value="related_to">相关</option>
            </select>
          </div>
          <div className="flex items-center space-x-2">
            <label className="text-sm font-medium text-gray-700">时间范围:</label>
            <select
              value={filters.timeRange}
              onChange={(e) => setFilters(prev => ({ ...prev, timeRange: e.target.value }))}
              className="px-3 py-1 border border-gray-300 rounded-md text-sm focus:ring-2 focus:ring-amber-500 focus:border-transparent"
            >
              <option value="all">全部</option>
              <option value="2024">2024年</option>
              <option value="2023">2023年</option>
              <option value="2022">2022年</option>
              <option value="recent">最近5年</option>
            </select>
          </div>
        </div>
      </div>

      {/* 主要内容区域 */}
      <div className="flex-1 overflow-auto p-4">
        {!graphData ? (
          <div className="text-center py-12">
            <div className="w-16 h-16 bg-amber-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <Filter className="w-8 h-8 text-amber-600" />
            </div>
            <h3 className="text-lg font-medium text-gray-900 mb-2">开始探索知识图谱</h3>
            <p className="text-gray-500 mb-6">输入搜索查询或使用筛选器来查看知识图谱</p>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3 max-w-2xl mx-auto">
              {[
                "显示所有机器学习相关的论文",
                "查找与深度学习相关的作者合作网络",
                "分析人工智能领域的研究趋势",
                "展示自然语言处理的关键技术"
              ].map((example, index) => (
                <button
                  key={index}
                  onClick={() => setSearchQuery(example)}
                  className="p-3 text-left bg-gray-50 hover:bg-gray-100 rounded-lg border border-gray-200 transition-colors duration-200"
                >
                  <p className="text-sm text-gray-700">{example}</p>
                </button>
              ))}
            </div>
          </div>
        ) : (
          <div className="space-y-6">
            {/* 知识图谱显示 */}
            <GraphDisplay data={graphData} />
            
            {/* 统计信息 */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div className="card">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm font-medium text-gray-600">节点数量</p>
                    <p className="text-2xl font-bold text-gray-900">
                      {graphData?.elements?.filter((el: any) => el.data.id && !el.data.source).length || 0}
                    </p>
                  </div>
                  <div className="w-12 h-12 bg-amber-100 rounded-full flex items-center justify-center">
                    <Filter className="w-6 h-6 text-amber-600" />
                  </div>
                </div>
              </div>
              
              <div className="card">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm font-medium text-gray-600">关系数量</p>
                    <p className="text-2xl font-bold text-gray-900">
                      {graphData?.elements?.filter((el: any) => el.data.source).length || 0}
                    </p>
                  </div>
                  <div className="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center">
                    <Filter className="w-6 h-6 text-blue-600" />
                  </div>
                </div>
              </div>
              
              <div className="card">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm font-medium text-gray-600">查询时间</p>
                    <p className="text-2xl font-bold text-gray-900">
                      {new Date().toLocaleTimeString()}
                    </p>
                  </div>
                  <div className="w-12 h-12 bg-green-100 rounded-full flex items-center justify-center">
                    <RefreshCw className="w-6 h-6 text-green-600" />
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
