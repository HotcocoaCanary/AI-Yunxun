"use client";

import { useState, useRef, useEffect } from "react";
import { ToolBox } from "@/components/tool-box";
import { Message } from "@/types/chat";

export default function ChatPage() {
    const [messages, setMessages] = useState<Message[]>([]);
    const [input, setInput] = useState("");
    const [isTyping, setIsTyping] = useState(false);
    const scrollRef = useRef<HTMLDivElement>(null);

    // 自动滚动到底部逻辑
    useEffect(() => {
        scrollRef.current?.scrollIntoView({ behavior: "smooth" });
    }, [messages]);

    const handleSubmit = async () => {
        if (!input.trim() || isTyping) return;

        const userMsg: Message = { role: 'user', content: input };
        const newMessages = [...messages, userMsg];

        // 1. 更新 UI，展示用户消息并预留助理回复位置
        setMessages([...newMessages, { role: 'assistant', content: '', tools: [] }]);
        setInput("");
        setIsTyping(true);

        try {
            const response = await fetch("/api/chat", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ messages: newMessages }),
            });

            if (!response.ok) throw new Error("网络请求失败");

            const reader = response.body?.getReader();
            const decoder = new TextDecoder();

            while (true) {
                const { done, value } = await reader!.read();
                if (done) break;

                const chunk = decoder.decode(value);
                // 智谱或自定义 SSE 可能以双换行符分隔数据包
                const lines = chunk.split("\n\n");

                lines.forEach(line => {
                    if (!line.trim() || !line.startsWith("data: ")) return;

                    try {
                        const { type, data } = JSON.parse(line.replace("data: ", ""));

                        setMessages(prev => {
                            const newMsgs = [...prev];
                            const lastMsg = { ...newMsgs[newMsgs.length - 1] };

                            if (type === "text") {
                                // 累加流式文本内容
                                lastMsg.content += data;
                            } else if (type === "tool_use") {
                                // 初始化工具状态
                                lastMsg.tools = [...(lastMsg.tools || []), {
                                    callId: data.callId,
                                    name: data.name,
                                    args: data.args,
                                    status: 'running'
                                }];
                            } else if (type === "tool_result") {
                                // 更新工具执行结果，并捕获 ui_type 标记
                                lastMsg.tools = lastMsg.tools?.map(t =>
                                    t.callId === data.callId ? {
                                        ...t,
                                        result: data.result,
                                        ui_type: data.ui_type, // 关键：存储 UI 渲染类型
                                        status: 'done'
                                    } : t
                                );
                            } else if (type === "error") {
                                lastMsg.content += `\n[Error: ${data}]`;
                            }

                            newMsgs[newMsgs.length - 1] = lastMsg;
                            return newMsgs;
                        });
                    } catch (e) {
                        console.error("解析 SSE 数据包失败:", e);
                    }
                });
            }
        } catch (err: any) {
            console.error("Stream error:", err);
            setMessages(prev => [
                ...prev.slice(0, -1),
                { role: 'assistant', content: `抱歉，发生了错误: ${err.message}` }
            ]);
        } finally {
            setIsTyping(false);
        }
    };

    return (
        <div className="flex flex-col h-screen bg-gray-50">
            {/* 顶部导航栏 (可选) */}
            <header className="p-4 border-b bg-white flex justify-between items-center">
                <h1 className="text-xl font-bold text-black">McpGraph Agent</h1>
                <div className="text-xs text-gray-400">Powered by GLM-4.7 & MCP</div>
            </header>

            {/* 聊天内容区域 */}
            <div className="flex-1 overflow-y-auto p-4 md:p-8">
                <div className="max-w-4xl mx-auto space-y-8">
                    {messages.map((msg, idx) => (
                        <div
                            key={idx}
                            className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}
                        >
                            <div className={`group relative max-w-[90%] md:max-w-[80%] ${
                                msg.role === 'user'
                                    ? 'bg-blue-600 text-white p-4 rounded-2xl rounded-tr-none shadow-sm'
                                    : 'text-black w-full'
                            }`}>
                                {/* 文本消息主体 */}
                                <div className={`whitespace-pre-wrap leading-relaxed ${
                                    msg.role === 'assistant' ? 'text-gray-800' : ''
                                }`}>
                                    {msg.content || (msg.role === 'assistant' && !msg.tools?.length && "...")}
                                </div>

                                {/* 工具渲染区 (仅助理角色展示) */}
                                {msg.role === 'assistant' && msg.tools && msg.tools.length > 0 && (
                                    <div className="mt-6 space-y-3">
                                        {msg.tools.map(tool => (
                                            <ToolBox key={tool.callId} tool={tool} />
                                        ))}
                                    </div>
                                )}
                            </div>
                        </div>
                    ))}
                    <div ref={scrollRef} className="h-4" />
                </div>
            </div>

            {/* 底部输入框区域 */}
            <div className="p-6 bg-white border-t shadow-2xl">
                <div className="max-w-3xl mx-auto relative flex items-center gap-3">
                    <input
                        className="flex-1 bg-gray-100 border-none rounded-2xl px-6 py-4 focus:outline-none focus:ring-2 focus:ring-blue-500 text-black placeholder-gray-400 transition-all"
                        placeholder="输入指令，例如：展示 TechCorp 的技术栈图谱..."
                        value={input}
                        onChange={(e) => setInput(e.target.value)}
                        onKeyDown={(e) => e.key === 'Enter' && handleSubmit()}
                        disabled={isTyping}
                    />
                    <button
                        onClick={handleSubmit}
                        disabled={isTyping || !input.trim()}
                        className={`p-4 rounded-xl transition-all ${
                            isTyping || !input.trim()
                                ? 'bg-gray-200 text-gray-400 cursor-not-allowed'
                                : 'bg-black text-white hover:scale-105 active:scale-95 shadow-lg'
                        }`}
                    >
                        <SendIcon />
                    </button>
                </div>
                <p className="text-[10px] text-center text-gray-400 mt-3">
                    Agent 可能会产生偏差，请核实重要信息。
                </p>
            </div>
        </div>
    );
}

// 简单的发送图标组件
function SendIcon() {
    return (
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <line x1="22" y1="2" x2="11" y2="13"></line>
            <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
        </svg>
    );
}