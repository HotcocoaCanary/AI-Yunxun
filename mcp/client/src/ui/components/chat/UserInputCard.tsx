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
    <div>
      <div>
        <button
          type="button"
          onClick={() => setDeepThinking((prev) => !prev)}
        >
          thought: {deepThinking ? "on" : "off"}
        </button>
        <button
          type="button"
          onClick={() => setWebSearch((prev) => !prev)}
        >
          web-search: {webSearch ? "on" : "off"}
        </button>
      </div>
      <textarea
        rows={1}
        value={input}
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
      <button onClick={handleSubmit} disabled={isTyping || !input.trim()}>
        Send
      </button>
    </div>
  );
}
