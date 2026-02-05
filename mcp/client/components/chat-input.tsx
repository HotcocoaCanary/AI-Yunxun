"use client";

import {useState} from "react";
import type {RefObject} from "react";
import {ChatMessageHandle} from "@/components/chat-message";

export function ChatInput({messageRef}: { messageRef: RefObject<ChatMessageHandle | null> }) {
    const [input, setInput] = useState("");
    const [isTyping, setIsTyping] = useState(false);

    const handleSubmit = async () => {
        if (!input.trim() || isTyping) return;

        const text = input;
        setInput("");
        setIsTyping(true);

        messageRef.current?.addUserMessage(text);
        messageRef.current?.startAssistantMessage();

        try {
            const history = messageRef.current?.getMessages() ?? [];
            const response = await fetch("/api/chat", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({messages: history}),
            });

            if (!response.ok) throw new Error("网络请求失败");

            const reader = response.body?.getReader();
            const decoder = new TextDecoder();

            while (true) {
                const {done, value} = await reader!.read();
                if (done) break;

                const chunk = decoder.decode(value);
                const lines = chunk.split("\n\n");

                lines.forEach(line => {
                    if (!line.trim() || !line.startsWith("data: ")) return;

                    try {
                        const {type, data} = JSON.parse(line.replace("data: ", ""));

                        if (type === "text") {
                            messageRef.current?.appendAssistantText(data);
                        } else if (type === "tool_use") {
                            messageRef.current?.addToolUse({
                                callId: data.callId,
                                name: data.name,
                                args: data.args,
                                status: "running"
                            });
                        } else if (type === "tool_result") {
                            messageRef.current?.addToolResult(data.callId, data.result, data.ui_type);
                        } else if (type === "error") {
                            messageRef.current?.addError(data);
                        }
                    } catch (e) {
                        console.error("解析 SSE 数据包失败:", e);
                    }
                });
            }
        } catch (err: any) {
            console.error("Stream error:", err);
            messageRef.current?.addError(err.message);
        } finally {
            setIsTyping(false);
        }
    };

    // 修改 return 部分
    return (
        <div className="relative group">
            <div
                className="relative flex items-end gap-2 bg-white border border-gray-200 p-2 rounded-[24px] shadow-[0_8px_30px_rgb(0,0,0,0.04)] focus-within:shadow-[0_8px_30px_rgb(0,0,0,0.08)] focus-within:border-gray-300 transition-all duration-300">
            <textarea
                rows={1}
                className="flex-1 max-h-40 bg-transparent border-none px-4 py-3 focus:outline-none text-[15px] text-gray-800 placeholder-gray-400 resize-none"
                placeholder="输入指令，探索图谱..."
                value={input}
                onChange={(e) => {
                    setInput(e.target.value);
                    e.target.style.height = 'auto';
                    e.target.style.height = e.target.scrollHeight + 'px';
                }}
                onKeyDown={(e) => {
                    if (e.key === 'Enter' && !e.shiftKey) {
                        e.preventDefault();
                        handleSubmit();
                    }
                }}
                disabled={isTyping}
            />
                <button
                    onClick={handleSubmit}
                    disabled={isTyping || !input.trim()}
                    className={`p-3 rounded-full transition-all duration-200 ${
                        isTyping || !input.trim()
                            ? 'bg-gray-50 text-gray-300'
                            : 'bg-black text-white hover:bg-gray-800 shadow-md'
                    }`}
                >
                    {isTyping ? <LoadingIcon/> : <SendIcon/>}
                </button>
            </div>
            <p className="text-[11px] text-center text-gray-400 mt-3 tracking-wide">
                Shift + Enter 换行 · McpGraph Agent V1.0
            </p>
        </div>
    );

// 增加一个小小的 Loading 动画
    function LoadingIcon() {
        return (
            <svg className="animate-spin h-5 w-5 text-gray-400" xmlns="http://www.w3.org/2000/svg" fill="none"
                 viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                <path className="opacity-75" fill="currentColor"
                      d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
        )
    }
}

function SendIcon() {
    return (
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none"
             stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <line x1="22" y1="2" x2="11" y2="13"></line>
            <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
        </svg>
    );
}
