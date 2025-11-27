import Link from "next/link";
import { ArrowRight, CheckCircle2, ShieldCheck, Sparkles } from "lucide-react";

export default function ProjectHero() {
  return (
    <section className="hero-banner section-card">
      <div className="hero-copy">
        <span className="hero-badge">Graph-first MCP Agent</span>
        <h1 className="hero-title">基于 Neo4j 的可插拔知识图谱智能体</h1>
        <p className="hero-desc">
          自动检索 / 爬取 → Mongo 入库 → 三元组写入 Neo4j → RAG 生成，流式输出文本、子图与图表，界面即时可视化。
        </p>
        <div className="hero-meta">
          <span className="pill">
            <Sparkles size={16} />
            图谱驱动工作流
          </span>
          <span className="pill">
            <ShieldCheck size={16} />
            工具可插拔 & 复用
          </span>
        </div>
        <div className="cta-group">
          <Link href="/chat" className="btn btn-primary">
            开始对话
            <ArrowRight size={16} />
          </Link>
          <Link href="/setting" className="btn btn-ghost">
            配置数据与工具
          </Link>
        </div>
      </div>
      <div className="mini-card">
        <p className="label">实时输出</p>
        <ul className="feature-list">
          <li>
            <CheckCircle2 size={16} />
            流式回答，边生成边推理
          </li>
          <li>
            <CheckCircle2 size={16} />
            返回当前回答的 Neo4j 子图，前端即时渲染
          </li>
          <li>
            <CheckCircle2 size={16} />
            可选图表结果，自动生成 Metabase 风格
          </li>
          <li>
            <CheckCircle2 size={16} />
            所有能力封装为 MCP Tool，随时插拔
          </li>
        </ul>
      </div>
    </section>
  );
}
