import { AppShell } from "@/components/layout/AppShell";
import Link from "next/link";

export default function Home() {
  return (
    <AppShell>
      <div className="mx-auto flex max-w-2xl flex-col items-start justify-center gap-3 py-10">
        <h1 className="text-2xl font-semibold text-zinc-900">
          欢迎使用 AI-Yunxun 前端工作台
        </h1>
        <p className="text-sm text-zinc-600">
          当前只是一个干净的骨架结构，后续会接入：聊天对话、MCP 工具调用、图表与图谱展示。
        </p>
        <p className="text-sm text-zinc-600">
          你可以先从{" "}
          <Link
            href="/chat"
            className="font-medium text-amber-600 underline-offset-2 hover:underline"
          >
            /chat
          </Link>{" "}
          页面开始迭代对话与图表界面。
        </p>
      </div>
    </AppShell>
  );
}
