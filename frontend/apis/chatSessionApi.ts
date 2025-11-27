import { ChatMessage, ChatTreeNode } from "@/types/chat";

export const ChatSessionApi = {
  async getTree(): Promise<ChatTreeNode[]> {
    const res = await fetch("/api/chat/tree", { cache: "no-store" });
    if (!res.ok) {
      throw new Error("获取聊天树失败");
    }
    return res.json();
  },
  async getMessages(sessionId: string): Promise<ChatMessage[]> {
    const res = await fetch(`/api/chat/session/${sessionId}/messages`, {
      cache: "no-store",
    });
    if (!res.ok) {
      throw new Error("获取聊天记录失败");
    }
    return res.json();
  },
};
