'use client';

import { useState, useEffect } from 'react';
import { BarChart3, TrendingUp, Users, FileText, Calendar, Filter } from 'lucide-react';
import ChartDisplay from './ChartDisplay';

interface AnalyticsData {
  totalPapers: number;
  totalAuthors: number;
  totalInstitutions: number;
  totalKeywords: number;
  papersByYear: any[];
  topAuthors: any[];
  topInstitutions: any[];
  topKeywords: any[];
  researchTrends: any[];
  collaborationNetwork: any[];
}

export default function Analytics() {
  const [analyticsData, setAnalyticsData] = useState<AnalyticsData | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [timeRange, setTimeRange] = useState('all');
  const [category, setCategory] = useState('all');

  useEffect(() => {
    loadAnalyticsData();
  }, [timeRange, category]);

  const loadAnalyticsData = async () => {
    setIsLoading(true);
    try {
      // 模拟从后端获取分析数据
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      const mockData: AnalyticsData = {
        totalPapers: 12543,
        totalAuthors: 3241,
        totalInstitutions: 156,
        totalKeywords: 892,
        papersByYear: [
          { year: '2020', count: 1200 },
          { year: '2021', count: 1450 },
          { year: '2022', count: 1680 },
          { year: '2023', count: 1920 },
          { year: '2024', count: 2100 }
        ],
        topAuthors: [
          { name: '张三', papers: 45, citations: 1200 },
          { name: '李四', papers: 38, citations: 980 },
          { name: '王五', papers: 35, citations: 850 },
          { name: '赵六', papers: 32, citations: 720 },
          { name: '钱七', papers: 28, citations: 650 }
        ],
        topInstitutions: [
          { name: '清华大学', papers: 320, authors: 45 },
          { name: '北京大学', papers: 280, authors: 38 },
          { name: '中科院', papers: 250, authors: 35 },
          { name: '复旦大学', papers: 220, authors: 32 },
          { name: '上海交大', papers: 200, authors: 28 }
        ],
        topKeywords: [
          { name: '机器学习', count: 1250, percentage: 15.2 },
          { name: '深度学习', count: 980, percentage: 11.9 },
          { name: '神经网络', count: 850, percentage: 10.3 },
          { name: '自然语言处理', count: 720, percentage: 8.7 },
          { name: '计算机视觉', count: 650, percentage: 7.9 }
        ],
        researchTrends: [
          { month: '1月', papers: 180 },
          { month: '2月', papers: 165 },
          { month: '3月', papers: 195 },
          { month: '4月', papers: 210 },
          { month: '5月', papers: 225 },
          { month: '6月', papers: 240 }
        ],
        collaborationNetwork: [
          { source: '清华大学', target: '北京大学', strength: 45 },
          { source: '清华大学', target: '中科院', strength: 38 },
          { source: '北京大学', target: '中科院', strength: 32 },
          { source: '复旦大学', target: '上海交大', strength: 28 }
        ]
      };
      
      setAnalyticsData(mockData);
    } catch (error) {
      console.error('加载分析数据失败:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const getPapersByYearChart = () => ({
    type: 'bar',
    title: '年度论文发表趋势',
    xAxis: analyticsData?.papersByYear.map(item => item.year) || [],
    yAxis: analyticsData?.papersByYear.map(item => item.count) || []
  });

  const getTopAuthorsChart = () => ({
    type: 'bar',
    title: '高产作者排行',
    xAxis: analyticsData?.topAuthors.map(item => item.name) || [],
    yAxis: analyticsData?.topAuthors.map(item => item.papers) || []
  });

  const getTopKeywordsChart = () => ({
    type: 'pie',
    title: '热门关键词分布',
    data: analyticsData?.topKeywords.map(item => ({
      name: item.name,
      value: item.count
    })) || []
  });

  const getResearchTrendsChart = () => ({
    type: 'line',
    title: '研究趋势分析',
    xAxis: analyticsData?.researchTrends.map(item => item.month) || [],
    yAxis: analyticsData?.researchTrends.map(item => item.papers) || []
  });

  if (isLoading) {
    return (
      <div className="h-full flex items-center justify-center">
        <div className="text-center">
          <div className="w-16 h-16 border-4 border-amber-500 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
          <p className="text-gray-600">正在加载分析数据...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="h-full flex flex-col bg-white">
      {/* 头部 */}
      <div className="border-b border-gray-200 p-4">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-xl font-semibold text-gray-900">数据分析</h2>
          <div className="flex space-x-2">
            <select
              value={timeRange}
              onChange={(e) => setTimeRange(e.target.value)}
              className="px-3 py-2 border border-gray-300 rounded-md text-sm focus:ring-2 focus:ring-amber-500 focus:border-transparent"
            >
              <option value="all">全部时间</option>
              <option value="2024">2024年</option>
              <option value="2023">2023年</option>
              <option value="recent">最近5年</option>
            </select>
            <select
              value={category}
              onChange={(e) => setCategory(e.target.value)}
              className="px-3 py-2 border border-gray-300 rounded-md text-sm focus:ring-2 focus:ring-amber-500 focus:border-transparent"
            >
              <option value="all">全部类别</option>
              <option value="ai">人工智能</option>
              <option value="ml">机器学习</option>
              <option value="nlp">自然语言处理</option>
              <option value="cv">计算机视觉</option>
            </select>
          </div>
        </div>
      </div>

      {/* 主要内容 */}
      <div className="flex-1 overflow-auto p-4">
        {/* 统计卡片 */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
          <div className="card">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">论文总数</p>
                <p className="text-2xl font-bold text-gray-900">{analyticsData?.totalPapers.toLocaleString()}</p>
              </div>
              <div className="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center">
                <FileText className="w-6 h-6 text-blue-600" />
              </div>
            </div>
          </div>
          
          <div className="card">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">作者总数</p>
                <p className="text-2xl font-bold text-gray-900">{analyticsData?.totalAuthors.toLocaleString()}</p>
              </div>
              <div className="w-12 h-12 bg-green-100 rounded-full flex items-center justify-center">
                <Users className="w-6 h-6 text-green-600" />
              </div>
            </div>
          </div>
          
          <div className="card">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">机构总数</p>
                <p className="text-2xl font-bold text-gray-900">{analyticsData?.totalInstitutions}</p>
              </div>
              <div className="w-12 h-12 bg-purple-100 rounded-full flex items-center justify-center">
                <BarChart3 className="w-6 h-6 text-purple-600" />
              </div>
            </div>
          </div>
          
          <div className="card">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">关键词总数</p>
                <p className="text-2xl font-bold text-gray-900">{analyticsData?.totalKeywords}</p>
              </div>
              <div className="w-12 h-12 bg-amber-100 rounded-full flex items-center justify-center">
                <TrendingUp className="w-6 h-6 text-amber-600" />
              </div>
            </div>
          </div>
        </div>

        {/* 图表区域 */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
          <ChartDisplay data={getPapersByYearChart()} />
          <ChartDisplay data={getTopAuthorsChart()} />
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
          <ChartDisplay data={getTopKeywordsChart()} />
          <ChartDisplay data={getResearchTrendsChart()} />
        </div>

        {/* 详细数据表格 */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* 高产作者 */}
          <div className="card">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">高产作者</h3>
            <div className="space-y-3">
              {analyticsData?.topAuthors.map((author, index) => (
                <div key={index} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                  <div className="flex items-center space-x-3">
                    <div className="w-8 h-8 bg-amber-500 text-white rounded-full flex items-center justify-center text-sm font-bold">
                      {index + 1}
                    </div>
                    <div>
                      <p className="font-medium text-gray-900">{author.name}</p>
                      <p className="text-sm text-gray-500">{author.citations} 次引用</p>
                    </div>
                  </div>
                  <div className="text-right">
                    <p className="font-semibold text-gray-900">{author.papers} 篇</p>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* 顶级机构 */}
          <div className="card">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">顶级机构</h3>
            <div className="space-y-3">
              {analyticsData?.topInstitutions.map((institution, index) => (
                <div key={index} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                  <div className="flex items-center space-x-3">
                    <div className="w-8 h-8 bg-blue-500 text-white rounded-full flex items-center justify-center text-sm font-bold">
                      {index + 1}
                    </div>
                    <div>
                      <p className="font-medium text-gray-900">{institution.name}</p>
                      <p className="text-sm text-gray-500">{institution.authors} 位作者</p>
                    </div>
                  </div>
                  <div className="text-right">
                    <p className="font-semibold text-gray-900">{institution.papers} 篇</p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
