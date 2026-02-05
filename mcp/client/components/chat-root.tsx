"use client";

import { useRef } from "react";
import { ChatMessage, ChatMessageHandle } from "@/components/chat-message";
import { ChatInput } from "@/components/chat-input";

export default function ChatRoot() {
  const messageRef = useRef<ChatMessageHandle>(null);
  return (
    <div className="flex h-screen bg-[#F4F4F5]">
      <div className="hidden md:flex w-64 bg-white border-r border-gray-200 flex-col p-6">
        <div className="flex items-center gap-3 mb-8">
          <div className="w-8 h-8 bg-blue-600 rounded-xl rotate-3 shadow-lg flex items-center justify-center text-white font-bold">G</div>
          <span className="font-bold tracking-tight text-gray-800">McpGraph</span>
        </div>
        <div className="space-y-4">
          <div className="text-[11px] font-bold text-gray-400 uppercase tracking-widest">最近对话</div>
          <div className="p-3 bg-gray-50 rounded-xl border border-gray-100 text-sm text-gray-600 cursor-pointer hover:bg-gray-100">新对话图谱...</div>
        </div>
      </div>
      <div className="flex-1 flex flex-col relative h-full">
        <header className="h-14 flex items-center justify-between px-8 bg-gradient-to-r from-white/70 to-white/30 backdrop-blur-md border-b border-gray-200/60 sticky top-0 z-20">
          <div className="flex items-center gap-3">
            <h2 className="text-sm font-semibold text-gray-800">GLM-4.7-Flash | 智谱开放平台</h2>
            <span className="text-[11px] text-gray-500">实时</span>
            <span className="w-2 h-2 rounded-full bg-green-500 animate-pulse" />
          </div>
          <div className="text-[11px] text-gray-500">McpGraph · v1.0</div>
        </header>
        <main className="flex-1 overflow-y-auto scroll-smooth custom-scrollbar px-6">
          <div className="max-w-[880px] mx-auto py-10">
            <ChatMessage ref={messageRef} />
          </div>
        </main>
        <div className="px-6 pb-8 pt-3 bg-gradient-to-t from-[#F4F4F5] via-[#F4F4F5] to-transparent">
          <div className="max-w-[880px] mx-auto">
            <ChatInput messageRef={messageRef} />
          </div>
        </div>
      </div>
    </div>
  );
}
