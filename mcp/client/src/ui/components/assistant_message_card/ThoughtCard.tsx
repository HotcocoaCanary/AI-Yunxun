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
        <span className="subcard-title">Thought</span>
        <span className="subcard-state">{open ? "Hide" : "Show"}</span>
      </button>
      {open && (
        <div className="subcard-body">
          <pre>{content}</pre>
        </div>
      )}
    </div>
  );
}
