import { Loader2 } from "lucide-react";

export default function AgentLoading() {
  return (
    <div className="flex min-h-screen items-center justify-center space-x-3 text-amber-600">
      <Loader2 className="h-5 w-5 animate-spin" />
      <span>智能体工作台加载中...</span>
    </div>
  );
}
