 "use client";

 import { useState } from "react";

 type Message = {
     role: "user" | "assistant";
     content: string;
 };

 export default function ChatPage() {
     const [messages, setMessages] = useState<Message[]>([]);
     const [input, setInput] = useState("");
     const [isLoading, setIsLoading] = useState(false);
     const [error, setError] = useState<string | null>(null);

     const sendMessage = async () => {
         const text = input.trim();
         if (!text || isLoading) return;

         const nextMessages: Message[] = [...messages, { role: "user", content: text }];
         setMessages(nextMessages);
         setInput("");
         setIsLoading(true);
         setError(null);

         try {
             const res = await fetch("/api/chat", {
                 method: "POST",
                 headers: { "Content-Type": "application/json" },
                 body: JSON.stringify({
                     messages: nextMessages.map(m => ({ role: m.role, content: m.content })),
                 }),
             });
             const data = await res.json();
             if (!res.ok) {
                 throw new Error(data?.error || "Request failed");
             }
             setMessages(prev => [...prev, { role: "assistant", content: data.content || "" }]);
         } catch (err: any) {
             setError(err?.message ?? "Unknown error");
         } finally {
             setIsLoading(false);
         }
     };

     return (
         <div className="chat-page">
             <div className="shell">
                 <header className="header">
                     <div className="brand">MCP Chat</div>
                     <div className="subtitle">Zhipu LLM • non-streaming</div>
                 </header>

                 <div className="panel">
                     <div className="messages">
                         {messages.length === 0 && (
                             <div className="empty">
                                 说点什么开始吧，比如：帮我总结 MCP 的用途。
                             </div>
                         )}
                         {messages.map((m, idx) => (
                             <div key={`${m.role}-${idx}`} className={`bubble ${m.role}`}>
                                 <div className="role">{m.role === "user" ? "你" : "助手"}</div>
                                 <div className="content">{m.content}</div>
                             </div>
                         ))}
                         {isLoading && (
                             <div className="bubble assistant">
                                 <div className="role">助手</div>
                                 <div className="content dim">正在思考…</div>
                             </div>
                         )}
                     </div>

                     <div className="composer">
                         <textarea
                             className="input"
                             placeholder="输入问题，回车发送（Shift+Enter 换行）"
                             value={input}
                             onChange={e => setInput(e.target.value)}
                             onKeyDown={e => {
                                 if (e.key === "Enter" && !e.shiftKey) {
                                     e.preventDefault();
                                     void sendMessage();
                                 }
                             }}
                         />
                         <div className="actions">
                             <button className="btn" onClick={sendMessage} disabled={isLoading}>
                                 {isLoading ? "发送中…" : "发送"}
                             </button>
                         </div>
                         {error && <div className="error">{error}</div>}
                     </div>
                 </div>
             </div>

             <style jsx>{`
                 :global(body) {
                     background: radial-gradient(1200px 800px at 10% 0%, #f4efe4 0%, #efe7db 45%, #e9ddcf 100%);
                     color: #1e1b16;
                 }
                 .chat-page {
                     min-height: 100vh;
                     padding: 48px 20px 64px;
                     display: flex;
                     justify-content: center;
                     font-family: "Space Grotesk", "Segoe UI", "Helvetica Neue", Arial, sans-serif;
                 }
                 .shell {
                     width: 100%;
                     max-width: 900px;
                 }
                 .header {
                     display: flex;
                     align-items: baseline;
                     gap: 16px;
                     margin-bottom: 16px;
                 }
                 .brand {
                     font-size: 28px;
                     font-weight: 700;
                     letter-spacing: 0.5px;
                 }
                 .subtitle {
                     font-size: 14px;
                     color: #5c5246;
                 }
                 .panel {
                     background: #fdf9f2;
                     border: 1px solid #e1d3c1;
                     border-radius: 18px;
                     box-shadow: 0 12px 30px rgba(45, 34, 20, 0.12);
                     overflow: hidden;
                 }
                 .messages {
                     padding: 20px 20px 8px;
                     min-height: 360px;
                 }
                 .empty {
                     color: #7b6d5f;
                     background: #f7efe4;
                     border: 1px dashed #d7c4ae;
                     padding: 16px;
                     border-radius: 12px;
                 }
                 .bubble {
                     margin: 10px 0;
                     padding: 12px 14px;
                     border-radius: 14px;
                     max-width: 85%;
                 }
                 .bubble.user {
                     margin-left: auto;
                     background: #1f2937;
                     color: #f3f4f6;
                 }
                 .bubble.assistant {
                     margin-right: auto;
                     background: #f3e6d5;
                     color: #2b241c;
                 }
                 .role {
                     font-size: 12px;
                     opacity: 0.7;
                     margin-bottom: 4px;
                 }
                 .content {
                     white-space: pre-wrap;
                     line-height: 1.5;
                 }
                 .content.dim {
                     opacity: 0.6;
                 }
                 .composer {
                     border-top: 1px solid #ead8c7;
                     padding: 16px;
                     background: #fbf6ee;
                 }
                 .input {
                     width: 100%;
                     min-height: 96px;
                     padding: 12px;
                     border-radius: 12px;
                     border: 1px solid #d9c8b5;
                     resize: vertical;
                     font-size: 14px;
                     background: #fffdf8;
                     color: #1e1b16;
                 }
                 .actions {
                     display: flex;
                     justify-content: flex-end;
                     margin-top: 12px;
                 }
                 .btn {
                     background: #b45309;
                     color: #fff;
                     border: none;
                     padding: 10px 18px;
                     border-radius: 999px;
                     cursor: pointer;
                     font-weight: 600;
                     letter-spacing: 0.3px;
                 }
                 .btn:disabled {
                     opacity: 0.6;
                     cursor: not-allowed;
                 }
                 .error {
                     margin-top: 10px;
                     color: #a61b1b;
                     font-size: 13px;
                 }
                 @media (max-width: 640px) {
                     .messages {
                         min-height: 300px;
                     }
                     .bubble {
                         max-width: 100%;
                     }
                 }
             `}</style>
         </div>
     );
 }
