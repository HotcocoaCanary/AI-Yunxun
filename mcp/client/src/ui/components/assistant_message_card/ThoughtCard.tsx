"use client";

import { useState } from "react";

export function ThoughtCard({ content }: { content: string }) {
  const [open, setOpen] = useState(true);

  if (!content) return null;

  return (
    <div className="subcard thought-card">
      <button
        type="button"
        className="subcard-toggle"
        aria-expanded={open}
        onClick={() => setOpen((prev) => !prev)}
      >
        <span className="subcard-chevron">{open ? "▼" : "▶"}</span>
        <span className="subcard-title">
          <span className="subcard-title-dot" />
          思考过程
        </span>
        <span className="subcard-state">{open ? "收起" : "展开"}</span>
      </button>
      {open && (
        <div className="subcard-body">
          <pre>{content}</pre>
        </div>
      )}
    </div>
  );
}
