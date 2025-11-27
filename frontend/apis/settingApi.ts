import { DataStats } from "@/types/setting";
import { McpToolStatus } from "@/types/mcp";

export const SettingApi = {
  async getDataStats(): Promise<DataStats> {
    const res = await fetch("/api/setting/stats", { cache: "no-store" });
    if (!res.ok) {
      throw new Error("获取数据统计失败");
    }
    return res.json();
  },
  async importFile(file: File): Promise<void> {
    const formData = new FormData();
    formData.append("file", file);
    const res = await fetch("/api/setting/import/file", {
      method: "POST",
      body: formData,
    });
    if (!res.ok) {
      throw new Error("文件上传失败");
    }
  },
  async importText(text: string): Promise<void> {
    const res = await fetch("/api/setting/import/text", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ text }),
    });
    if (!res.ok) {
      throw new Error("文本导入失败");
    }
  },
  async importUrl(url: string): Promise<void> {
    const res = await fetch("/api/setting/import/url", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ url }),
    });
    if (!res.ok) {
      throw new Error("URL 导入失败");
    }
  },
  async getTools(): Promise<McpToolStatus[]> {
    const res = await fetch("/api/tools", { cache: "no-store" });
    if (!res.ok) {
      throw new Error("加载工具列表失败");
    }
    return res.json();
  },
  async toggleTool(name: string, enabled: boolean): Promise<void> {
    const res = await fetch("/api/tools/toggle", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ name, enabled }),
    });
    if (!res.ok) {
      throw new Error("切换工具状态失败");
    }
  },
};
