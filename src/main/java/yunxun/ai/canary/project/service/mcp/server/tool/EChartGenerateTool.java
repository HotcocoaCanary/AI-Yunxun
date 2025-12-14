package yunxun.ai.canary.project.service.mcp.server.tool;

import org.springframework.stereotype.Component;

/**
 * ECharts 图表生成工具
 * 将图表生成能力暴露给前端 AI 代理，通过 MCP/SSE 接口调用
 * 负责生成 ECharts 可绘制的图表数据配置
 * 支持的图表类型：
 * - bar: 柱状图
 * - line: 折线图
 * - pie: 饼图
 * - graph: 力导向图（用于图谱可视化）
 * - scatter: 散点图
 */
@Component
public class EChartGenerateTool {

}

