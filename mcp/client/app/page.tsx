"use client";

import { useState, useRef } from "react";
import { ToolBox } from "@/components/tool-box";
import {Message} from "@/types/chat";

export default function ChatPage() {
    const [messages, setMessages] = useState<Message[]>([]);
    const [input, setInput] = useState("");
    const [isTyping, setIsTyping] = useState(false);
    const scrollRef = useRef<HTMLDivElement>(null);

    const handleSubmit = async () => {
        if (!input.trim()) return;

        const userMsg: Message = { role: 'user', content: input };
        const newMessages = [...messages, userMsg];
        setMessages(newMessages);
        setInput("");
        setIsTyping(true);

        // 预读一条空助理消息，准备填充流
        setMessages(prev => [...prev, { role: 'assistant', content: '', tools: [] }]);

        try {
            const response = await fetch("/api/chat", {
                method: "POST",
                body: JSON.stringify({ messages: newMessages }),
            });

            const reader = response.body?.getReader();
            const decoder = new TextDecoder();

            while (true) {
                const { done, value } = await reader!.read();
                if (done) break;

                const chunk = decoder.decode(value);
                const lines = chunk.split("\n\n");

                lines.forEach(line => {
                    if (!line.startsWith("data: ")) return;
                    const { type, data } = JSON.parse(line.replace("data: ", ""));

                    setMessages(prev => {
                        const lastMsg = { ...prev[prev.length - 1] };

                        if (type === "text") {
                            lastMsg.content += data;
                        } else if (type === "tool_use") {
                            lastMsg.tools = [...(lastMsg.tools || []), {
                                callId: data.callId,
                                name: data.name,
                                args: data.args,
                                status: 'running'
                            }];
                        } else if (type === "tool_result") {
                            lastMsg.tools = lastMsg.tools?.map(t =>
                                t.callId === data.callId ? { ...t, result: data.result, status: 'done' } : t
                            );
                        }

                        return [...prev.slice(0, -1), lastMsg];
                    });
                });

                // 自动滚动到底部
                scrollRef.current?.scrollIntoView({ behavior: "smooth" });
            }
        } catch (err) {
            console.error("Stream error:", err);
        } finally {
            setIsTyping(false);
        }
    };

    return (
        <div className="flex flex-col h-screen bg-white">
            {/* 聊天区域 */}
            <div className="flex-1 overflow-y-auto p-4 space-y-6">
                {messages.map((msg, idx) => (
                    <div key={idx} className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}>
                        <div className={`max-w-[80%] ${msg.role === 'user' ? 'bg-blue-600 text-white p-3 rounded-lg' : ''}`}>
                            {/* 文本内容 */}
                            <div className="whitespace-pre-wrap">{msg.content}</div>

                            {/* 工具调用展示区 (Cherry Studio 风格) */}
                            {msg.tools && msg.tools.length > 0 && (
                                <div className="mt-4 space-y-2">
                                    {msg.tools.map(tool => (
                                        <ToolBox key={tool.callId} tool={tool} />
                                    ))}
                                </div>
                            )}
                        </div>
                    </div>
                ))}
                <div ref={scrollRef} />
            </div>

            {/* 输入区域 */}
            <div className="p-4 border-t">
                <div className="max-w-3xl mx-auto flex gap-2">
                    <input
                        className="flex-1 border rounded-full px-4 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 text-black"
                        placeholder="问问 Neo4j 或生成图表..."
                        value={input}
                        onChange={(e) => setInput(e.target.value)}
                        onKeyDown={(e) => e.key === 'Enter' && handleSubmit()}
                    />
                    <button
                        onClick={handleSubmit}
                        disabled={isTyping}
                        className="bg-black text-white rounded-full px-6 py-2 disabled:bg-gray-300"
                    >
                        发送
                    </button>
                </div>
            </div>
        </div>
    );
}