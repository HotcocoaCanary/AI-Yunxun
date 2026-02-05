"use client";

import {forwardRef, useEffect, useImperativeHandle, useRef, useState} from "react";
import {ToolBox} from "@/components/tool-box";
import {ThoughtBox} from "@/components/thought-box";
import {Message, ToolInvocation} from "@/types/chat";

export interface ChatMessageHandle {
    addUserMessage: (content: string) => void;
    startAssistantMessage: () => void;
    appendAssistantText: (content: string) => void;
    setThinking: (content: string) => void;
    addToolUse: (tool: ToolInvocation) => void;
    addToolResult: (callId: string, result: string) => void;
    addError: (message: string) => void;
    getMessages: () => Message[];
}

export const ChatMessage = forwardRef<ChatMessageHandle>(function ChatMessage(_props, ref) {
    const [messages, setMessages] = useState<Message[]>([]);
    const messagesRef = useRef<Message[]>([]);
    const scrollRef = useRef<HTMLDivElement>(null);

    const syncMessages = (updater: (prev: Message[]) => Message[]) => {
        const next = updater(messagesRef.current);
        messagesRef.current = next;
        setMessages(next);
    };

    useEffect(() => {
        scrollRef.current?.scrollIntoView({behavior: "smooth"});
    }, [messages]);

    useImperativeHandle(ref, () => ({
        addUserMessage: (content) => {
            syncMessages(prev => [...prev, {role: "user", content}]);
        },
        startAssistantMessage: () => {
            syncMessages(prev => [...prev, {role: "assistant", content: "", tools: []}]);
        },
        appendAssistantText: (content) => {
            syncMessages(prev => {
                if (prev.length === 0) return prev;
                const newMsgs = [...prev];
                const last = {...newMsgs[newMsgs.length - 1]};
                last.content += content;
                newMsgs[newMsgs.length - 1] = last;
                return newMsgs;
            });
        },
        setThinking: (content) => {
            if (!content) return;
            syncMessages(prev => {
                if (prev.length === 0) return prev;
                const newMsgs = [...prev];
                const last = {...newMsgs[newMsgs.length - 1]};
                const existing = last.thinking ?? "";
                last.thinking = existing ? existing + content : content;
                newMsgs[newMsgs.length - 1] = last;
                return newMsgs;
            });
        },
        addToolUse: (tool) => {
            syncMessages(prev => {
                if (prev.length === 0) return prev;
                const newMsgs = [...prev];
                const last = {...newMsgs[newMsgs.length - 1]};
                last.tools = [...(last.tools || []), tool];
                newMsgs[newMsgs.length - 1] = last;
                return newMsgs;
            });
        },
        addToolResult: (callId, result) => {
            syncMessages(prev => {
                if (prev.length === 0) return prev;
                const newMsgs = [...prev];
                const last = {...newMsgs[newMsgs.length - 1]};
                last.tools = last.tools?.map(t =>
                    t.callId === callId ? {...t, result, status: "done"} : t
                );
                newMsgs[newMsgs.length - 1] = last;
                return newMsgs;
            });
        },
        addError: (message) => {
            syncMessages(prev => {
                if (prev.length === 0) return prev;
                const newMsgs = [...prev];
                const last = {...newMsgs[newMsgs.length - 1]};
                last.content += `\n[Error: ${message}]`;
                newMsgs[newMsgs.length - 1] = last;
                return newMsgs;
            });
        },
        getMessages: () => messagesRef.current
    }), []);

    // 只展示渲染部分的修改点，逻辑保持不变
    return (
        <div className="space-y-8 py-10 px-4">
            {messages.map((message, idx) => (
                /* 注意这里：我加了 key={idx} */
                <div
                    key={`msg-${idx}`}
                    className={`flex w-full mb-6 animate-slide-up ${message.role === 'user' ? 'justify-end' : 'justify-start'}`}
                >
                    <div className={`group relative max-w-[80%] ${message.role === 'user' ? 'order-1' : 'order-2'}`}>

                        {/* 用户消息：深色高级感 */}
                        {message.role === 'user' ? (
                            <div className="bg-gradient-to-br from-[#111113] to-[#1f1f24] text-white px-5 py-3 rounded-[20px] rounded-br-none shadow-lg text-[15px] leading-relaxed ring-1 ring-gray-800">
                                {message.content}
                            </div>
                        ) : (
                            /* AI 消息：卡片感 */
                            <div className="bg-white border border-gray-200/80 shadow-premium p-6 rounded-[22px] rounded-bl-none transition-shadow hover:shadow-[0_10px_35px_rgba(0,0,0,0.06)]">
                                <div className="flex items-center gap-2 mb-3">
                                    <div className="w-6 h-6 bg-blue-100 rounded-lg flex items-center justify-center">
                                        <svg className="w-4 h-4 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M13 10V3L4 14h7v7l9-11h-7z"/>
                                        </svg>
                                    </div>
                                    <span className="text-[11px] font-semibold text-gray-500 tracking-tight">助理回复</span>
                                </div>

                                <div className="text-[#3f3f46] text-[15px] leading-[1.8] space-y-4">
                                    {message.content || "..."}
                                </div>

                                {message.role === 'assistant' && (message.thinking?.trim() || (message.tools && message.tools.length > 0)) && (
                                    <div className="mt-5 pt-5 border-t border-gray-100 space-y-4">
                                        {message.thinking?.trim() && (
                                            <ThoughtBox content={message.thinking} />
                                        )}
                                        {message.tools?.map((tool, tIdx) => (
                                            <ToolBox key={tool.callId || tIdx} tool={tool} />
                                        ))}
                                    </div>
                                )}
                            </div>
                        )}
                    </div>
                </div>
            ))}
            <div ref={scrollRef}/>
        </div>
    );
});
