"use client";

import { useState } from "react";

export function ThoughtCard({ content }: { content: string }) {
  const [open, setOpen] = useState(true);

  if (!content) return null;

  return (
    <div>
      <button type="button" onClick={() => setOpen((prev) => !prev)}>
        Thought {open ? "▾" : "▸"}
      </button>
      {open && <pre>{content}</pre>}
    </div>
  );
}
