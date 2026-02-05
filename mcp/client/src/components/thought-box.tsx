"use client";

export function ThoughtBox({ content }: { content: string }) {
  if (!content) return null;

  return (
    <div>
      <pre>{content}</pre>
    </div>
  );
}
