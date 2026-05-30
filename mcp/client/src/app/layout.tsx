// app/layout.tsx
import './globals.css';

export const metadata = {
    title: 'AI-Yunxun 智能图谱助手',
    description: '基于 MCP 协议和 EChart 的智能图谱问答助手',
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
    return (
        <html lang="zh-CN">
        <body>{children}</body>
        </html>
    );
}
