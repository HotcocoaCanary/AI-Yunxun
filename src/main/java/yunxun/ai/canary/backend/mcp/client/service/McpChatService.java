package yunxun.ai.canary.backend.mcp.client.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import yunxun.ai.canary.backend.mcp.client.model.ToolCallInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 高级聊天服务，使用智谱AI适配器
 * <p>
 * 主要职责：
 * <ul>
 *     <li>通过模型驱动 MCP 工具调用（Neo4j 图谱、图表服务等）</li>
 *     <li>提取结构化结果（图谱 JSON / 图表 JSON）供前端使用</li>
 *     <li>基于工具结果生成自然语言回答</li>
 * </ul>
 */
@Service
public class McpChatService {

    /**
     * 返回给 REST 层的结果
     *
     * @param replyText 给用户的自然语言回答
     * @param graphJson 可选的图谱 JSON（nodes/edges）用于 GraphPanel
     * @param chartJson 可选的图表 JSON（ChartResponse）用于 ChartPanel
     * @param toolCalls 工具调用列表
     */
    public record ChatResult(String replyText, String graphJson, String chartJson, List<ToolCallInfo> toolCalls) {
        public ChatResult(String replyText, String graphJson, String chartJson) {
            this(replyText, graphJson, chartJson, new ArrayList<>());
        }
    }

    /**
     * 流式事件类型
     */
    public enum StreamEventType {
        TOOL_CALL,  // 工具调用
        CONTENT,    // 文本内容
        GRAPH,      // 图谱数据
        CHART,      // 图表数据
        DONE        // 完成
    }

    /**
     * 流式事件
     */
    public record ChatStreamEvent(
            StreamEventType type,
            ToolCallInfo toolCall,
            String content,
            String graphJson,
            String chartJson
    ) {
        public static ChatStreamEvent toolCall(ToolCallInfo toolCall) {
            return new ChatStreamEvent(StreamEventType.TOOL_CALL, toolCall, null, null, null);
        }

        public static ChatStreamEvent content(String content) {
            return new ChatStreamEvent(StreamEventType.CONTENT, null, content, null, null);
        }

        public static ChatStreamEvent graph(String graphJson) {
            return new ChatStreamEvent(StreamEventType.GRAPH, null, null, graphJson, null);
        }

        public static ChatStreamEvent chart(String chartJson) {
            return new ChatStreamEvent(StreamEventType.CHART, null, null, null, chartJson);
        }

        public static ChatStreamEvent done() {
            return new ChatStreamEvent(StreamEventType.DONE, null, null, null, null);
        }
    }

    private final ZhipuAiChatAdapter chatAdapter;

    public McpChatService(ZhipuAiChatAdapter chatAdapter) {
        this.chatAdapter = chatAdapter;
    }

    /**
     * 执行单轮对话调用，使用底层模型和工具
     * <p>
     * 阶段一：让模型调用 MCP 工具，只输出结构化 JSON 标记：
     * <ul>
     *     <li>GRAPH_JSON: {"nodes":[...], "edges":[...]}</li>
     *     <li>CHART_JSON: { ChartResponse ... }</li>
     * </ul>
     * 阶段二：将提取的 JSON 反馈给模型，生成自然语言回答，不向用户暴露原始 JSON
     */
    public ChatResult chat(String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("消息不能为空");
        }

        // ===== 阶段一：工具调用 + 仅输出 JSON =====
        String systemPrompt1 = """
                你是"云寻"知识助手，当前阶段的任务是：
                - 根据用户问题，调用可用的 MCP 工具（特别是 Neo4j 图谱工具和图表服务工具）；
                - 最终【只输出结构化 JSON 标记】，不要输出任何自然语言解释。

                可用的工具：
                1. 网络搜索工具（如果已配置外部 MCP 搜索服务器）：
                   - web_search 或 search_web: 执行网络搜索，获取实时数据
                   - 注意：需要先配置外部 MCP 搜索服务器（如 websearch-mcp）
                   
                2. Neo4j 图谱工具（neo4j_*）：
                   - neo4j_find_node: 查询节点
                   - neo4j_find_relationship: 查询关系
                   - neo4j_create_node: 创建节点
                   - neo4j_create_relationship: 创建关系
                   - 其他 CRUD 操作...
                   
                3. MongoDB 工具（mongo_*）：
                   - mongo_find_by_topic: 按主题查询文档
                   - mongo_find_by_id: 按 ID 查询文档
                   - mongo_save_document: 保存文档
                   - 其他 CRUD 操作...
                   
                4. 图表生成工具：
                   - generate_chart_from_data: 根据原始数据生成 ECharts 图表（推荐使用！）
                   
                图表生成工作流程（重要！）：
                当用户问题涉及数据分析或趋势图表时（例如："近10年考研人数变化趋势"、"各学科论文数量分布"），
                请按以下步骤操作：
                
                步骤 1：网络搜索数据（如果已配置搜索工具）
                - 首先调用网络搜索工具（web_search 或 search_web），获取相关的统计数据
                - 搜索关键词应包含：问题主题 + 时间范围 + "数据"/"统计"/"官方"
                - 例如："近10年考研人数" 应搜索："2015-2024 考研人数 统计数据 官方"
                - 从搜索结果中提取关键数据点（年份、数值等）
                - 如果未配置搜索工具，可以基于你的知识库回答，但应明确说明数据来源
                
                步骤 2：整理数据格式
                - 将搜索到的数据整理为 JSON 数组格式
                - 时间序列数据格式：[{"year": "2015", "count": 150}, {"year": "2016", "count": 170}, ...]
                - 分类数据格式：[{"category": "机器学习", "value": 50}, {"category": "深度学习", "value": 30}, ...]
                - 字段命名建议：
                  * X 轴字段（类别/时间）：year, date, time, name, category, label
                  * Y 轴字段（数值）：count, value, amount, quantity, number
                
                步骤 3：调用图表生成工具
                - 使用 generate_chart_from_data 工具
                - 参数：title（图表标题）、dataJson（整理好的 JSON 数组）、chartType（可选，bar/line/pie）
                - 工具会自动识别字段名并生成 ECharts 图表配置
                
                注意事项：
                - 优先使用官方或权威数据源（如国家统计局、教育部等）
                - 如果无法获取准确数据，如实说明，不要编造
                - 数据点数量建议在 5-50 之间
                - 图表类型：时间序列用 line（折线图），分类对比用 bar（柱状图），占比用 pie（饼图）
                - 如果搜索工具不可用，可以在回答中说明"由于无法访问实时数据，以下为示例数据"

                约定的输出标记：
                1) 若问题涉及实体/关系图谱（例如"Bob 认识谁？"、"查找与机器学习相关的论文"），
                   请调用 neo4j_find_relationship 或 neo4j_find_node 查询 Neo4j 图谱，
                   然后将查询结果转换为前端需要的格式，并输出一行：
                   GRAPH_JSON: {"nodes":[...], "edges":[...]}
                   其中：
                     - nodes: [{ "id": string, "label": string, "type": string }]
                     - edges: [{ "id": string, "source": string, "target": string, "label": string }]
                   注意：如果 Neo4j 返回的是原始格式，你需要将其转换为上述格式。

                2) 若问题涉及数值分析或趋势图（例如"近 10 年考研人数变化趋势，画一个柱状图"），
                   请调用 generate_chart 工具，
                   并输出一行：
                   CHART_JSON: { ...ChartResponse JSON... }

                3) 若一个问题同时需要图谱和图表，你可以输出最多两行：
                   - 一行以 GRAPH_JSON: 开头
                   - 一行以 CHART_JSON: 开头

                非常重要：
                - 不要输出任何自然语言解释；
                - 不要输出除 GRAPH_JSON/CHART_JSON 以外的内容；
                - 每个标记占一行，后面紧跟合法的 JSON；
                - 如果查询 Neo4j 返回的数据格式不是 {nodes, edges}，需要先转换格式。
                """;
        String toolsRaw = chatAdapter.chat(systemPrompt1, message);

        String graphJson = extractMarkerJson(toolsRaw, "GRAPH_JSON:");
        String chartJson = extractMarkerJson(toolsRaw, "CHART_JSON:");

        // ===== 阶段二：基于工具结果生成自然语言回答 =====
        String replyText;
        if (graphJson != null || chartJson != null) {
            StringBuilder userPrompt = new StringBuilder();
            userPrompt.append("用户原始问题: ").append(message).append("\n");
            if (graphJson != null) {
                userPrompt.append("图谱 JSON: ").append(graphJson).append("\n");
            }
            if (chartJson != null) {
                userPrompt.append("图表 JSON: ").append(chartJson).append("\n");
            }

            String systemPrompt2 = """
                    你是"云寻"知识助手。
                    你已经通过工具获得了结构化结果（图谱 JSON、图表 JSON），
                    现在需要基于这些结果用自然语言回答用户问题。

                    要求：
                    - 用清晰、简洁的中文回答；
                    - 如果有图谱 JSON，可以简要说明有哪些实体及其关系；
                    - 如果有图表 JSON，可以结合其中的数据描述整体趋势和结论；
                    - 不要输出任何 JSON 文本；
                    - 不要输出 GRAPH_JSON 或 CHART_JSON 之类的标记。
                    """;
            replyText = chatAdapter.chat(systemPrompt2, userPrompt.toString());
        } else {
            // 回退：没有结构化输出，执行普通的问答调用
            String systemPrompt3 = """
                    你是"云寻"知识助手，能够根据需要调用可用的 MCP 工具
                    （例如 MongoDB、Neo4j 图谱、图表服务等）来完成任务。
                    在需要读取或写入数据、生成图表时优先通过工具，而不是凭空编造。
                    回答时用自然语言回答用户问题，不要输出任何 GRAPH_JSON 或 CHART_JSON 标记。
                    """;
            replyText = chatAdapter.chat(systemPrompt3, message);
        }

        // 提取工具调用信息（从同步调用中无法直接获取，这里返回空列表）
        // 在流式调用中可以实时获取
        return new ChatResult(replyText, graphJson, chartJson, new ArrayList<>());
    }

    /**
     * 响应式聊天调用（返回 Mono）
     *
     * @param message 用户消息
     * @return Mono 包装的聊天结果
     */
    public Mono<ChatResult> chatReactive(String message) {
        return Mono.fromCallable(() -> chat(message))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 流式对话调用，支持实时工具调用追踪
     * 简化实现：先执行同步调用获取结果，然后流式返回
     */
    public Flux<ChatStreamEvent> chatStream(String message) {
        if (message == null || message.isBlank()) {
            return Flux.error(new IllegalArgumentException("消息不能为空"));
        }

        System.out.println("McpChatService.chatStream 开始处理消息: " + message);
        
        try {
            // 阶段一：工具调用（使用响应式方法）
            System.out.println("开始阶段一：工具调用");
            String toolsRaw = chatAdapter.chatReactive(getSystemPrompt(), message)
                    .block(); // 在流式调用中，我们需要等待工具调用结果
            System.out.println("阶段一完成，工具调用结果长度: " + (toolsRaw != null ? toolsRaw.length() : 0));
            if (toolsRaw != null && !toolsRaw.isEmpty()) {
                System.out.println("工具调用结果预览: " + toolsRaw.substring(0, Math.min(200, toolsRaw.length())));
            }

            String graphJson = extractMarkerJson(toolsRaw, "GRAPH_JSON:");
            String chartJson = extractMarkerJson(toolsRaw, "CHART_JSON:");
            System.out.println("提取结果 - graphJson: " + (graphJson != null ? "有" : "无") + ", chartJson: " + (chartJson != null ? "有" : "无"));

            // 从工具调用结果中提取工具名（简化实现）
            List<ToolCallInfo> toolCalls = extractToolCallsFromContent(toolsRaw);
            System.out.println("提取到工具调用数量: " + toolCalls.size());

            // 发送工具调用事件
            Flux<ChatStreamEvent> toolCallEvents = Flux.fromIterable(toolCalls)
                    .map(ChatStreamEvent::toolCall)
                    .doOnNext(event -> System.out.println("发送工具调用事件: " + event.toolCall().toolName()));

        // 阶段二：基于工具结果生成自然语言回答
        String finalPrompt;
        if (graphJson != null || chartJson != null) {
            StringBuilder userPrompt = new StringBuilder();
            userPrompt.append("用户原始问题: ").append(message).append("\n");
            if (graphJson != null) {
                userPrompt.append("图谱 JSON: ").append(graphJson).append("\n");
            }
            if (chartJson != null) {
                userPrompt.append("图表 JSON: ").append(chartJson).append("\n");
            }
            finalPrompt = userPrompt.toString();
        } else {
            finalPrompt = message;
        }

        String systemPrompt = (graphJson != null || chartJson != null) ? """
                你是"云寻"知识助手。
                你已经通过工具获得了结构化结果（图谱 JSON、图表 JSON），
                现在需要基于这些结果用自然语言回答用户问题。

                要求：
                - 用清晰、简洁的中文回答；
                - 如果有图谱 JSON，可以简要说明有哪些实体及其关系；
                - 如果有图表 JSON，可以结合其中的数据描述整体趋势和结论；
                - 不要输出任何 JSON 文本；
                - 不要输出 GRAPH_JSON 或 CHART_JSON 之类的标记。
                """ : """
                你是"云寻"知识助手，能够根据需要调用可用的 MCP 工具
                （例如 MongoDB、Neo4j 图谱、图表服务等）来完成任务。
                在需要读取或写入数据、生成图表时优先通过工具，而不是凭空编造。
                回答时用自然语言回答用户问题，不要输出任何 GRAPH_JSON 或 CHART_JSON 标记。
                """;

            // 流式生成回答
            System.out.println("开始阶段二：生成自然语言回答");
            System.out.println("系统提示词长度: " + systemPrompt.length());
            System.out.println("用户提示词长度: " + finalPrompt.length());
            
            Flux<ChatStreamEvent> contentEvents = chatAdapter.chatStream(systemPrompt, finalPrompt)
                    .doOnNext(content -> System.out.println("收到流式内容片段: [" + content + "], 长度: " + (content != null ? content.length() : 0)))
                    .map(content -> {
                        if (content != null && !content.isBlank()) {
                            ChatStreamEvent event = ChatStreamEvent.content(content);
                            System.out.println("创建内容事件，内容: [" + content + "]");
                            return event;
                        }
                        System.out.println("跳过空内容片段");
                        return null;
                    })
                    .filter(event -> event != null)
                    .doOnNext(event -> System.out.println("发送内容事件，类型: " + event.type() + ", 内容长度: " + (event.content() != null ? event.content().length() : 0)))
                    .doOnComplete(() -> System.out.println("内容事件流完成"))
                    .doOnError(e -> {
                        System.err.println("内容事件流出错: " + e.getMessage());
                        e.printStackTrace();
                    });

            // 组合所有事件
            // 先构建可能的事件列表，过滤掉 null 值
            List<ChatStreamEvent> finalEvents = new ArrayList<>();
            if (graphJson != null) {
                finalEvents.add(ChatStreamEvent.graph(graphJson));
                System.out.println("添加图谱事件");
            }
            if (chartJson != null) {
                finalEvents.add(ChatStreamEvent.chart(chartJson));
                System.out.println("添加图表事件");
            }
            finalEvents.add(ChatStreamEvent.done());
            System.out.println("添加完成事件");
            
            System.out.println("开始组合所有事件流");
            return toolCallEvents
                    .concatWith(contentEvents)
                    .concatWith(Flux.fromIterable(finalEvents))
                    .doOnNext(event -> System.out.println("发送事件: " + event.type()))
                    .doOnComplete(() -> System.out.println("所有事件流完成"))
                    .doOnError(e -> {
                        System.err.println("事件流出错: " + e.getMessage());
                        e.printStackTrace();
                    });
        } catch (Exception e) {
            System.err.println("chatStream 处理出错: " + e.getMessage());
            e.printStackTrace();
            return Flux.error(e);
        }
    }

    /**
     * 从内容中提取工具调用信息（简化实现）
     */
    private List<ToolCallInfo> extractToolCallsFromContent(String content) {
        List<ToolCallInfo> toolCalls = new ArrayList<>();
        if (content == null || content.isBlank()) {
            return toolCalls;
        }

        // 检查是否包含工具调用标记
        String[] toolPatterns = {
                "neo4j_", "mongo_", "echart_", "generate_chart", "web_search", "search_web"
        };

        for (String pattern : toolPatterns) {
            if (content.contains(pattern)) {
                // 尝试提取工具名
                int idx = content.indexOf(pattern);
                if (idx >= 0) {
                    String remaining = content.substring(idx);
                    String toolName = remaining.split("[\\s(]")[0];
                    if (toolName.contains(pattern)) {
                        toolCalls.add(new ToolCallInfo(
                                ToolCallInfo.inferToolGroup(toolName),
                                toolName,
                                Map.of()
                        ));
                    }
                }
            }
        }

        return toolCalls;
    }

    /**
     * 获取系统提示词
     */
    private String getSystemPrompt() {
        return """
                你是"云寻"知识助手，当前阶段的任务是：
                - 根据用户问题，调用可用的 MCP 工具（特别是 Neo4j 图谱工具和图表服务工具）；
                - 最终【只输出结构化 JSON 标记】，不要输出任何自然语言解释。

                可用的工具：
                1. 网络搜索工具（如果已配置外部 MCP 搜索服务器）：
                   - web_search 或 search_web: 执行网络搜索，获取实时数据
                   - 注意：需要先配置外部 MCP 搜索服务器（如 websearch-mcp）
                   
                2. Neo4j 图谱工具（neo4j_*）：
                   - neo4j_find_node: 查询节点
                   - neo4j_find_relationship: 查询关系
                   - neo4j_create_node: 创建节点
                   - neo4j_create_relationship: 创建关系
                   - neo4j_find_path: 路径查询
                   - neo4j_find_neighbors: 邻居查询
                   - neo4j_fuzzy_search: 模糊查询
                   - 其他 CRUD 操作...
                   
                3. MongoDB 工具（mongo_*）：
                   - mongo_find_by_topic: 按主题查询文档
                   - mongo_find_by_id: 按 ID 查询文档
                   - mongo_save_document: 保存文档
                   - 其他 CRUD 操作...
                   
                4. 图表生成工具：
                   - echart_generate: 根据原始数据生成 ECharts 图表（支持 bar, line, pie, graph, scatter）

                图表生成工作流程（重要！）：
                当用户问题涉及数据分析或趋势图表时（例如："近10年考研人数变化趋势"、"各学科论文数量分布"），
                请按以下步骤操作：
                
                步骤 1：网络搜索数据（如果已配置搜索工具）
                - 首先调用网络搜索工具（web_search 或 search_web），获取相关的统计数据
                - 搜索关键词应包含：问题主题 + 时间范围 + "数据"/"统计"/"官方"
                - 例如："近10年考研人数" 应搜索："2015-2024 考研人数 统计数据 官方"
                - 从搜索结果中提取关键数据点（年份、数值等）
                - 如果未配置搜索工具，可以基于你的知识库回答，但应明确说明数据来源
                
                步骤 2：整理数据格式
                - 将搜索到的数据整理为 JSON 数组格式
                - 时间序列数据格式：[{"year": "2015", "count": 150}, {"year": "2016", "count": 170}, ...]
                - 分类数据格式：[{"category": "机器学习", "value": 50}, {"category": "深度学习", "value": 30}, ...]
                - 字段命名建议：
                  * X 轴字段（类别/时间）：year, date, time, name, category, label
                  * Y 轴字段（数值）：count, value, amount, quantity, number
                
                步骤 3：调用图表生成工具
                - 使用 echart_generate 工具
                - 参数：title（图表标题）、dataJson（整理好的 JSON 数组）、chartType（可选，bar/line/pie/graph/scatter）
                - 工具会自动识别字段名并生成 ECharts 图表配置
                
                注意事项：
                - 优先使用官方或权威数据源（如国家统计局、教育部等）
                - 如果无法获取准确数据，如实说明，不要编造
                - 数据点数量建议在 5-50 之间
                - 图表类型：时间序列用 line（折线图），分类对比用 bar（柱状图），占比用 pie（饼图），图谱用 graph（力导向图），相关性分析用 scatter（散点图）
                - 如果搜索工具不可用，可以在回答中说明"由于无法访问实时数据，以下为示例数据"

                约定的输出标记：
                1) 若问题涉及实体/关系图谱（例如"Bob 认识谁？"、"查找与机器学习相关的论文"），
                   请调用 neo4j_find_relationship 或 neo4j_find_node 查询 Neo4j 图谱，
                   然后将查询结果转换为前端需要的格式，并输出一行：
                   GRAPH_JSON: {"nodes":[...], "edges":[...]}
                   其中：
                     - nodes: [{ "id": string, "label": string, "type": string }]
                     - edges: [{ "id": string, "source": string, "target": string, "label": string }]
                   注意：如果 Neo4j 返回的是原始格式，你需要将其转换为上述格式。

                2) 若问题涉及数值分析或趋势图（例如"近 10 年考研人数变化趋势，画一个柱状图"），
                   请调用 echart_generate 工具，
                   并输出一行：
                   CHART_JSON: { ...ChartResponse JSON... }

                3) 若一个问题同时需要图谱和图表，你可以输出最多两行：
                   - 一行以 GRAPH_JSON: 开头
                   - 一行以 CHART_JSON: 开头

                非常重要：
                - 不要输出任何自然语言解释；
                - 不要输出除 GRAPH_JSON/CHART_JSON 以外的内容；
                - 每个标记占一行，后面紧跟合法的 JSON；
                - 如果查询 Neo4j 返回的数据格式不是 {nodes, edges}，需要先转换格式。
                """;
    }

    /**
     * 从模型输出中提取指定标记后的 JSON 载荷
     *
     * @param raw    完整的模型输出
     * @param marker 标记前缀，如 "GRAPH_JSON:" 或 "CHART_JSON:"
     * @return 修剪后的 JSON 字符串，如果未找到则返回 null
     */
    private String extractMarkerJson(String raw, String marker) {
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        int idx = raw.indexOf(marker);
        if (idx < 0) {
            return null;
        }
        String after = raw.substring(idx + marker.length()).trim();
        return after.isEmpty() ? null : after;
    }
}

