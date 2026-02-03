// app/layout.tsx
import './globals.css';

export const metadata = {
    title: 'MCP Web Console',
    description: 'Next.js + MCP Client Demo',
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
    return (
        <html lang="en">
        <body>{children}</body>
        </html>
    );
}
