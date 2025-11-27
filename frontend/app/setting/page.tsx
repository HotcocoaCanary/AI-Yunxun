'use client';
import { useEffect, useState } from "react";
import DataStatsPanel from "@/components/setting/DataStatsPanel";
import DataImportPanel from "@/components/setting/DataImportPanel";
import McpToolsManager from "@/components/setting/McpToolsManager";
import { SettingApi } from "@/apis/settingApi";
import { useSettingStore } from "@/store/settingStore";

export default function SettingPage() {
  const TABS = [
    { key: "stats", title: "数据统计", desc: "监控 Neo4j / Mongo 指标" },
    { key: "import", title: "数据导入", desc: "文件 / 文本 / URL" },
    { key: "tools", title: "工具管理", desc: "MCP 工具启用与来源" },
  ];

  const [activeTab, setActiveTab] = useState<string>("stats");
  const {
    stats,
    tools,
    loadingStats,
    loadingTools,
    setStats,
    setTools,
    setLoadingStats,
    setLoadingTools,
  } = useSettingStore();
  const [importing, setImporting] = useState(false);

  const loadStats = async () => {
    setLoadingStats(true);
    try {
      const data = await SettingApi.getDataStats();
      setStats(data);
    } catch (error) {
      console.error(error);
    } finally {
      setLoadingStats(false);
    }
  };

  const loadTools = async () => {
    setLoadingTools(true);
    try {
      const data = await SettingApi.getTools();
      setTools(data);
    } catch (error) {
      console.error(error);
    } finally {
      setLoadingTools(false);
    }
  };

  useEffect(() => {
    loadStats();
    loadTools();
  }, []);

  const handleImport =
    (importer: (value: string | File) => Promise<void>) => async (value: string | File) => {
      setImporting(true);
      try {
        await importer(value);
        await loadStats();
      } finally {
        setImporting(false);
      }
    };

  const renderPanel = () => {
    switch (activeTab) {
      case "stats":
        return <DataStatsPanel stats={stats} loading={loadingStats} onRefresh={loadStats} />;
      case "import":
        return (
          <DataImportPanel
            loading={importing}
            onUploadFile={handleImport((file) => SettingApi.importFile(file as File))}
            onSubmitText={handleImport((text) => SettingApi.importText(text as string))}
            onSubmitUrl={handleImport((url) => SettingApi.importUrl(url as string))}
          />
        );
      case "tools":
        return (
          <McpToolsManager
            tools={tools}
            loading={loadingTools}
            onToggleTool={async (name, enabled) => {
              await SettingApi.toggleTool(name, enabled);
              await loadTools();
            }}
          />
        );
      default:
        return null;
    }
  };

  return (
    <main className="setting-shell">
      <div className="setting-layout">
        <aside className="setting-nav">
          {TABS.map((tab) => (
            <button
              key={tab.key}
              type="button"
              className={`nav-item ${activeTab === tab.key ? "active" : ""}`}
              onClick={() => setActiveTab(tab.key)}
            >
              <span className="nav-title">{tab.title}</span>
              <span className="nav-desc">{tab.desc}</span>
            </button>
          ))}
        </aside>
        <div className="setting-content">{renderPanel()}</div>
      </div>
    </main>
  );
}
