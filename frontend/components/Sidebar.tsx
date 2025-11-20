'use client';

import { 
  MessageCircle, 
  Network, 
  Database, 
  BarChart3, 
  Settings,
  HelpCircle
} from 'lucide-react';
type TabType = 'chat' | 'graph' | 'data' | 'analytics';

interface SidebarProps {
  activeTab: TabType;
  onTabChange: (tab: TabType) => void;
}

const navigationItems = [
  {
    id: 'chat' as TabType,
    name: '智能问答',
    icon: MessageCircle,
    description: '与AI进行自然语言对话'
  },
  {
    id: 'graph' as TabType,
    name: '知识图谱',
    icon: Network,
    description: '可视化知识图谱网络'
  },
  {
    id: 'data' as TabType,
    name: '数据管理',
    icon: Database,
    description: '上传下载三元组数据'
  },
  {
    id: 'analytics' as TabType,
    name: '数据分析',
    icon: BarChart3,
    description: '查看统计图表分析'
  }
];

export default function Sidebar({ activeTab, onTabChange }: SidebarProps) {
  return (
    <div className="w-64 bg-white border-r border-gray-200 flex flex-col">
      {/* Logo和标题 */}
      <div className="p-6 border-b border-gray-200">
        <div className="flex items-center space-x-3">
          <div className="w-8 h-8 bg-gradient-to-br from-amber-400 to-amber-600 rounded-lg flex items-center justify-center">
            <Network className="w-5 h-5 text-white" />
          </div>
          <div>
            <h1 className="text-lg font-semibold text-gray-900">AI-Yunxun</h1>
            <p className="text-xs text-gray-500">智能知识图谱系统</p>
          </div>
        </div>
      </div>

      {/* 导航菜单 */}
      <nav className="flex-1 p-4 space-y-2">
        {navigationItems.map((item) => {
          const Icon = item.icon;
          const isActive = activeTab === item.id;
          
          return (
            <button
              key={item.id}
              onClick={() => onTabChange(item.id)}
              className={`w-full flex items-center space-x-3 px-3 py-3 rounded-lg text-left transition-all duration-200 group ${
                isActive
                  ? 'bg-amber-50 border border-amber-200 text-amber-700'
                  : 'hover:bg-gray-50 text-gray-700 hover:text-gray-900'
              }`}
            >
              <Icon 
                className={`w-5 h-5 ${
                  isActive ? 'text-amber-600' : 'text-gray-400 group-hover:text-gray-600'
                }`} 
              />
              <div className="flex-1 min-w-0">
                <p className={`text-sm font-medium ${
                  isActive ? 'text-amber-700' : 'text-gray-900'
                }`}>
                  {item.name}
                </p>
                <p className={`text-xs ${
                  isActive ? 'text-amber-600' : 'text-gray-500'
                }`}>
                  {item.description}
                </p>
              </div>
            </button>
          );
        })}
      </nav>

      {/* 底部操作 */}
      <div className="p-4 border-t border-gray-200 space-y-2">
        <button className="w-full flex items-center space-x-3 px-3 py-2 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors duration-200">
          <Settings className="w-4 h-4 text-gray-400" />
          <span className="text-sm">设置</span>
        </button>
        <button className="w-full flex items-center space-x-3 px-3 py-2 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors duration-200">
          <HelpCircle className="w-4 h-4 text-gray-400" />
          <span className="text-sm">帮助</span>
        </button>
      </div>
    </div>
  );
}
