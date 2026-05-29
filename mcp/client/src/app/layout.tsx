// app/layout.tsx
import './globals.css';

export const metadata = {
    title: '云巡助手 - AI 智能运维平台',
    description: '基于 DeepSeek V4 Pro 和 MCP 协议的智能运维助手',
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
    return (
        <html lang="zh-CN">
        <body>{children}</body>
        </html>
    );
}
