"use client";

import { useState } from "react";

export function UserInputCard({
  isTyping,
  onSubmit,
}: {
  isTyping: boolean;
  onSubmit: (payload: {
    text: string;
    deepThinking: boolean;
  }) => void;
}) {
  const [input, setInput] = useState("");
  const [deepThinking, setDeepThinking] = useState(true);

  const handleSubmit = () => {
    if (!input.trim() || isTyping) return;
    const text = input;
    setInput("");
    onSubmit({ text, deepThinking });
  };

  return (
    <div className="chat-composer">
      <div className="composer-actions">
        <button
          type="button"
          className="toggle-btn"
          data-active={deepThinking}
          aria-pressed={deepThinking}
          onClick={() => setDeepThinking((prev) => !prev)}
        >
          {deepThinking ? "🧠 深度思考已开启" : "深度思考已关闭"}
        </button>
      </div>
      <textarea
        rows={1}
        className="composer-input"
        value={input}
        placeholder="输入消息，按 Enter 发送..."
        onChange={(e) => {
          setInput(e.target.value);
          e.target.style.height = "auto";
          e.target.style.height = `${e.target.scrollHeight}px`;
        }}
        onKeyDown={(e) => {
          if (e.key === "Enter" && !e.shiftKey) {
            e.preventDefault();
            handleSubmit();
          }
        }}
        disabled={isTyping}
      />
      <div className="composer-footer">
        <div className="composer-hint">Shift + Enter 换行</div>
        <button
          className="btn btn-primary"
          onClick={handleSubmit}
          disabled={isTyping || !input.trim()}
        >
          {isTyping ? (
            <>
              <span className="spinner" />
              发送中
            </>
          ) : (
            "发送"
          )}
        </button>
      </div>
    </div>
  );
}
