"use client";

import { useRef } from "react";
import { ChatMessage, ChatMessageHandle } from "@/components/chat-message";
import { ChatInput } from "@/components/chat-input";
export default function ChatPage() {
    const messageRef = useRef<ChatMessageHandle>(null);

    return (
        <div className="flex h-screen bg-[#F4F4F5]">
            {/* 左侧装饰性侧边栏 (可选，增加专业感) */}
            <div className="hidden md:flex w-64 bg-white border-r border-gray-200 flex-col p-6">
                <div className="flex items-center gap-3 mb-8">
                    <div className="w-8 h-8 bg-blue-600 rounded-xl rotate-3 shadow-lg flex items-center justify-center text-white font-bold">G</div>
                    <span className="font-bold tracking-tight text-gray-800">McpGraph</span>
                </div>
                <div className="space-y-4">
                    <div className="text-[11px] font-bold text-gray-400 uppercase tracking-widest">Recent Chats</div>
                    <div className="p-3 bg-gray-50 rounded-xl border border-gray-100 text-sm text-gray-600 cursor-pointer">新对话图谱...</div>
                </div>
            </div>

            {/* 主聊天区 */}
            <div className="flex-1 flex flex-col relative h-full">
                <header className="h-16 flex items-center justify-between px-8 bg-white/50 backdrop-blur-md border-b border-gray-200/50 sticky top-0 z-20">
                    <div className="flex items-center gap-4">
                        <h2 className="text-sm font-semibold text-gray-700">知识图谱分析模式</h2>
                        <span className="w-2 h-2 rounded-full bg-green-500 animate-pulse" />
                    </div>
                </header>

                <main className="flex-1 overflow-y-auto scroll-smooth custom-scrollbar px-4">
                    {/* 增加一个限制宽度的容器，并给足上下间距 */}
                    <div className="max-w-3xl mx-auto py-12">
                        <ChatMessage ref={messageRef} />
                    </div>
                </main>

                {/* 输入框：悬浮感设计 */}
                <div className="px-4 pb-8 pt-2 bg-gradient-to-t from-[#F4F4F5] via-[#F4F4F5] to-transparent">
                    <div className="max-w-3xl mx-auto">
                        <ChatInput messageRef={messageRef} />
                    </div>
                </div>
            </div>
        </div>
    );
}