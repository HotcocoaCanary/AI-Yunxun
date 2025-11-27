import type { Metadata } from "next";
import { Space_Grotesk } from "next/font/google";
import "@/public/styles/globals.css";
import "@/public/styles/ui.css";

const grotesk = Space_Grotesk({ subsets: ["latin"], weight: ["400", "500", "600", "700"] });

export const metadata: Metadata = {
  title: "AI-Yunxun | 图谱驱动的 MCP 智能体",
  description:
    "基于 Mongo + Neo4j + MCP 的可插拔知识图谱智能体，支持流式输出文本 / 子图 / 图表，帮助团队快速构建图谱化问答体验。",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="zh-CN">
      <body className={grotesk.className}>
        <div className="min-h-screen bg-slate-50">{children}</div>
      </body>
    </html>
  );
}
