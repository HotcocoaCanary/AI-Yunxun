import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "MCP 对话与图谱",
  description: "Next.js 客户端：对话 + SSE + 图谱直接调用",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="zh-CN">
      <body>
        {children}
      </body>
    </html>
  );
}
