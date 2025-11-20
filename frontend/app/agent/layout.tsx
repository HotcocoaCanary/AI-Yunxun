import type { ReactNode } from "react";

export default function AgentLayout({ children }: { children: ReactNode }) {
  return (
    <section className="min-h-screen bg-gray-50">
      <div className="mx-auto max-w-7xl px-4 py-6 sm:px-6 lg:px-8">{children}</div>
    </section>
  );
}
