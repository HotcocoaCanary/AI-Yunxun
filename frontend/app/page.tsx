'use client';

import { useState } from 'react';
import Sidebar from '@/components/Sidebar';
import ChatInterface from '@/components/ChatInterface';
import KnowledgeGraph from '@/components/KnowledgeGraph';
import DataManagement from '@/components/DataManagement';
import Analytics from '@/components/Analytics';

export type TabType = 'chat' | 'graph' | 'data' | 'analytics';

export default function Home() {
  const [activeTab, setActiveTab] = useState<TabType>('chat');

  const renderContent = () => {
    switch (activeTab) {
      case 'chat':
        return <ChatInterface />;
      case 'graph':
        return <KnowledgeGraph />;
      case 'data':
        return <DataManagement />;
      case 'analytics':
        return <Analytics />;
      default:
        return <ChatInterface />;
    }
  };

  return (
    <div className="flex h-screen bg-gray-50">
      {/* 侧边栏 */}
      <Sidebar activeTab={activeTab} onTabChange={setActiveTab} />
      
      {/* 主内容区域 */}
      <main className="flex-1 flex flex-col overflow-hidden">
        <div className="flex-1 overflow-auto">
          {renderContent()}
        </div>
      </main>
    </div>
  );
}