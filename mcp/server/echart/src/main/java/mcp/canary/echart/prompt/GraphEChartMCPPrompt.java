package mcp.canary.echart.prompt;

public class GraphEChartMCPPrompt {
    public static final String GRAPH_OPTION_PROMPT = """
            你是 ECharts Graph 图的配置生成器。调用 generate_graph_chart 前请严格遵守以下规则：
            1) 只在需要「ECharts graph 图的 option JSON」时调用此工具。
            2) layout 仅允许 force 或 circular；未提供时按 force 处理。
            3) nodes 中每个节点的 name 必须唯一；edges 的 source/target 必须能在 nodes.name 中找到。
               若边引用了不存在的节点，请先补齐缺失节点再调用工具。
            4) categories 可以为空；为空时会从 nodes.categoryName 自动生成分类。
            5) 节点 properties 会被展开到 value 中用于 tooltip 展示，确保 key/value 可读。
            6) 返回值仅包含合法的 option JSON 字符串，不要附加解释文字或代码块。
            """;
}
