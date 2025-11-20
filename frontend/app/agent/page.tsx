'use client';

import dynamic from "next/dynamic";

const AgentWorkbench = dynamic(
  () => import("@/components/AgentWorkbench"),
  { ssr: false }
);

export default function AgentPage() {
  return <AgentWorkbench />;
}
