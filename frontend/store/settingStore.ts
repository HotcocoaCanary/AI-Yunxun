import { create } from "zustand";
import { DataStats } from "@/types/setting";
import { McpToolStatus } from "@/types/mcp";

interface SettingState {
  stats: DataStats | null;
  tools: McpToolStatus[];
  loadingStats: boolean;
  loadingTools: boolean;
  setStats: (stats: DataStats) => void;
  setTools: (tools: McpToolStatus[]) => void;
  setLoadingStats: (loading: boolean) => void;
  setLoadingTools: (loading: boolean) => void;
}

export const useSettingStore = create<SettingState>((set) => ({
  stats: null,
  tools: [],
  loadingStats: false,
  loadingTools: false,
  setStats: (stats) => set({ stats }),
  setTools: (tools) => set({ tools }),
  setLoadingStats: (loading) => set({ loadingStats: loading }),
  setLoadingTools: (loading) => set({ loadingTools: loading }),
}));
