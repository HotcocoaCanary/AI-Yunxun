'use client';

import { useEffect } from "react";

export default function AgentError({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  useEffect(() => {
    console.error("Agent workspace error:", error);
  }, [error]);

  return (
    <div className="flex min-h-screen flex-col items-center justify-center space-y-4 text-center">
      <div className="text-lg font-semibold text-red-600">
        智能体工作台加载失败
      </div>
      <p className="text-sm text-gray-500">
        {error.message || "请稍后再试，或点击下方按钮重新加载。"}
      </p>
      <button
        type="button"
        onClick={reset}
        className="rounded-lg bg-amber-500 px-4 py-2 text-sm font-medium text-white transition hover:bg-amber-600"
      >
        重新加载
      </button>
    </div>
  );
}
