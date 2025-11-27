import { BarChart3, Network, PlugZap, RefreshCw } from "lucide-react";

const FEATURES = [
  {
    icon: RefreshCw,
    title: "自动更新",
    desc: "按需触发搜索 / 爬虫，写入 Mongo 与 Neo4j，保持数据持续新鲜。",
  },
  {
    icon: Network,
    title: "图谱优先",
    desc: "Neo4j 子图作为核心上下文，输出标准化图谱 JSON，前端即可交互。",
  },
  {
    icon: PlugZap,
    title: "MCP 可插拔",
    desc: "所有能力封装为 MCP Tool，支持按来源启用 / 禁用，安全复用。",
  },
  {
    icon: BarChart3,
    title: "多模态回传",
    desc: "同时返回文本、图谱与图表，界面即时渲染，不再等待切换视图。",
  },
];

export default function FeatureHighlights() {
  return (
    <section className="section-card">
      <div className="section-head">
        <p className="chip">Highlights</p>
        <h2 className="section-title">核心特性</h2>
      </div>
      <div className="feature-grid">
        {FEATURES.map((item) => (
          <div key={item.title} className="feature-card">
            <span className="icon-pill">
              <item.icon size={18} />
            </span>
            <div>
              <p className="feature-title">{item.title}</p>
              <p className="feature-desc">{item.desc}</p>
            </div>
          </div>
        ))}
      </div>
    </section>
  );
}
