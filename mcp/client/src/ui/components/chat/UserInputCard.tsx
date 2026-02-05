"use client";

import { useState } from "react";

export function UserInputCard({
  isTyping,
  onSubmit
}: {
  isTyping: boolean;
  onSubmit: (payload: {
    text: string;
    deepThinking: boolean;
    webSearch: boolean;
  }) => void;
}) {
  const [input, setInput] = useState("");
  const [deepThinking, setDeepThinking] = useState(true);
  const [webSearch, setWebSearch] = useState(false);

  const handleSubmit = () => {
    if (!input.trim() || isTyping) return;
    const text = input;
    setInput("");
    onSubmit({ text, deepThinking, webSearch });
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
          Deep thinking {deepThinking ? "on" : "off"}
        </button>
        <button
          type="button"
          className="toggle-btn"
          data-active={webSearch}
          aria-pressed={webSearch}
          onClick={() => setWebSearch((prev) => !prev)}
        >
          Web search {webSearch ? "on" : "off"}
        </button>
      </div>
      <textarea
        rows={1}
        className="composer-input"
        value={input}
        placeholder="Type a message and press Enter to send..."
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
        <div className="composer-hint">Shift + Enter for a new line</div>
        <button
          className="btn btn-primary"
          onClick={handleSubmit}
          disabled={isTyping || !input.trim()}
        >
          Send
        </button>
      </div>
    </div>
  );
}
