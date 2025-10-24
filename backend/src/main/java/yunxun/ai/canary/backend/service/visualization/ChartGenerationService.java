package yunxun.ai.canary.backend.service.visualization;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import yunxun.ai.canary.backend.model.dto.QueryIntent;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 图表生成服务 - 负责生成各种可视化图表数据
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChartGenerationService {
    
    /**
     * 生成图谱可视化数据
     */
    public Map<String, Object> generateGraphVisualization(List<Map<String, Object>> graphResults) {
        try {
            log.info("生成图谱可视化数据: {} 个节点", graphResults.size());
            
            Map<String, Object> visualization = new HashMap<>();
            
            // 提取节点和边
            List<Map<String, Object>> nodes = new ArrayList<>();
            List<Map<String, Object>> edges = new ArrayList<>();
            Set<String> nodeIds = new HashSet<>();
            
            for (Map<String, Object> result : graphResults) {
                // 处理节点
                if (result.containsKey("n")) {
                    Map<String, Object> node = (Map<String, Object>) result.get("n");
                    String nodeId = extractNodeId(node);
                    if (nodeId != null && !nodeIds.contains(nodeId)) {
                        nodes.add(createNodeData(node, nodeId));
                        nodeIds.add(nodeId);
                    }
                }
                
                // 处理边
                if (result.containsKey("r") && result.containsKey("target")) {
                    Map<String, Object> edge = createEdgeData(result);
                    if (edge != null) {
                        edges.add(edge);
                    }
                }
            }
            
            visualization.put("nodes", nodes);
            visualization.put("edges", edges);
            visualization.put("layout", "force");
            visualization.put("type", "graph");
            
            log.info("图谱可视化数据生成完成: {} 个节点, {} 条边", nodes.size(), edges.size());
            return visualization;
            
        } catch (Exception e) {
            log.error("生成图谱可视化数据失败", e);
            return createEmptyVisualization();
        }
    }
    
    /**
     * 生成分析图表
     */
    public Map<String, Object> generateAnalysisCharts(List<Map<String, Object>> data, QueryIntent intent) {
        try {
            log.info("生成分析图表: intent={}, data={}", intent.getType(), data.size());
            
            Map<String, Object> charts = new HashMap<>();
            
            switch (intent.getType()) {
                case DATA_ANALYSIS:
                    charts.put("barChart", generateBarChart(data));
                    charts.put("pieChart", generatePieChart(data));
                    break;
                    
                case TREND_ANALYSIS:
                    charts.put("lineChart", generateLineChart(data));
                    charts.put("areaChart", generateAreaChart(data));
                    break;
                    
                default:
                    charts.put("barChart", generateBarChart(data));
            }
            
            return charts;
            
        } catch (Exception e) {
            log.error("生成分析图表失败", e);
            return new HashMap<>();
        }
    }
    
    /**
     * 生成趋势图表
     */
    public Map<String, Object> generateTrendCharts(List<Map<String, Object>> literature) {
        try {
            log.info("生成趋势图表: {} 条文献", literature.size());
            
            Map<String, Object> charts = new HashMap<>();
            
            // 按年份统计论文数量
            Map<String, Long> yearCount = literature.stream()
                    .filter(item -> item.get("publishedDate") != null)
                    .collect(Collectors.groupingBy(
                            item -> extractYear((String) item.get("publishedDate")),
                            Collectors.counting()
                    ));
            
            // 生成折线图数据
            List<Map<String, Object>> lineData = yearCount.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> {
                        Map<String, Object> point = new HashMap<>();
                        point.put("year", entry.getKey());
                        point.put("count", entry.getValue());
                        return point;
                    })
                    .collect(Collectors.toList());
            
            charts.put("lineChart", Map.of(
                    "type", "line",
                    "title", "论文发表趋势",
                    "data", lineData,
                    "xAxis", "year",
                    "yAxis", "count"
            ));
            
            // 生成作者合作网络
            Map<String, Object> networkData = generateAuthorNetwork(literature);
            charts.put("networkChart", networkData);
            
            return charts;
            
        } catch (Exception e) {
            log.error("生成趋势图表失败", e);
            return new HashMap<>();
        }
    }
    
    /**
     * 生成趋势分析图表
     */
    public Map<String, Object> generateTrendAnalysis(List<Map<String, Object>> trendData, QueryIntent intent) {
        try {
            log.info("生成趋势分析图表: {} 条数据", trendData.size());
            
            Map<String, Object> charts = new HashMap<>();
            
            // 按实体分组统计
            Map<String, List<Map<String, Object>>> entityGroups = trendData.stream()
                    .filter(item -> item.get("entity") != null)
                    .collect(Collectors.groupingBy(item -> (String) item.get("entity")));
            
            // 生成多线图
            List<Map<String, Object>> series = new ArrayList<>();
            for (Map.Entry<String, List<Map<String, Object>>> entry : entityGroups.entrySet()) {
                Map<String, Object> seriesData = new HashMap<>();
                seriesData.put("name", entry.getKey());
                seriesData.put("data", entry.getValue().stream()
                        .map(item -> Arrays.asList(
                                item.get("date"),
                                item.get("mentions")
                        ))
                        .collect(Collectors.toList()));
                series.add(seriesData);
            }
            
            charts.put("multiLineChart", Map.of(
                    "type", "multiLine",
                    "title", "实体提及趋势",
                    "series", series,
                    "xAxis", "date",
                    "yAxis", "mentions"
            ));
            
            return charts;
            
        } catch (Exception e) {
            log.error("生成趋势分析图表失败", e);
            return new HashMap<>();
        }
    }
    
    /**
     * 创建节点数据
     */
    private Map<String, Object> createNodeData(Map<String, Object> node, String nodeId) {
        Map<String, Object> nodeData = new HashMap<>();
        nodeData.put("id", nodeId);
        nodeData.put("label", extractNodeLabel(node));
        nodeData.put("type", extractNodeType(node));
        nodeData.put("size", calculateNodeSize(node));
        nodeData.put("color", getNodeColor(extractNodeType(node)));
        return nodeData;
    }
    
    /**
     * 创建边数据
     */
    private Map<String, Object> createEdgeData(Map<String, Object> result) {
        try {
            Map<String, Object> edge = new HashMap<>();
            edge.put("source", extractSourceId(result));
            edge.put("target", extractTargetId(result));
            edge.put("label", extractRelationshipType(result));
            edge.put("weight", 1);
            return edge;
        } catch (Exception e) {
            log.warn("创建边数据失败", e);
            return null;
        }
    }
    
    /**
     * 生成柱状图
     */
    private Map<String, Object> generateBarChart(List<Map<String, Object>> data) {
        Map<String, Long> counts = data.stream()
                .filter(item -> item.get("type") != null)
                .collect(Collectors.groupingBy(
                        item -> (String) item.get("type"),
                        Collectors.counting()
                ));
        
        List<Map<String, Object>> chartData = counts.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", entry.getKey());
                    item.put("value", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());
        
        return Map.of(
                "type", "bar",
                "title", "实体类型分布",
                "data", chartData
        );
    }
    
    /**
     * 生成饼图
     */
    private Map<String, Object> generatePieChart(List<Map<String, Object>> data) {
        Map<String, Long> counts = data.stream()
                .filter(item -> item.get("type") != null)
                .collect(Collectors.groupingBy(
                        item -> (String) item.get("type"),
                        Collectors.counting()
                ));
        
        List<Map<String, Object>> chartData = counts.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", entry.getKey());
                    item.put("value", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());
        
        return Map.of(
                "type", "pie",
                "title", "实体类型占比",
                "data", chartData
        );
    }
    
    /**
     * 生成折线图
     */
    private Map<String, Object> generateLineChart(List<Map<String, Object>> data) {
        // 按时间排序
        List<Map<String, Object>> sortedData = data.stream()
                .filter(item -> item.get("date") != null)
                .sorted(Comparator.comparing(item -> (String) item.get("date")))
                .collect(Collectors.toList());
        
        return Map.of(
                "type", "line",
                "title", "趋势分析",
                "data", sortedData,
                "xAxis", "date",
                "yAxis", "frequency"
        );
    }
    
    /**
     * 生成面积图
     */
    private Map<String, Object> generateAreaChart(List<Map<String, Object>> data) {
        return Map.of(
                "type", "area",
                "title", "累积趋势",
                "data", data,
                "xAxis", "date",
                "yAxis", "frequency"
        );
    }
    
    /**
     * 生成作者合作网络
     */
    private Map<String, Object> generateAuthorNetwork(List<Map<String, Object>> literature) {
        Map<String, Set<String>> authorCoauthors = new HashMap<>();
        
        for (Map<String, Object> paper : literature) {
            List<String> authors = (List<String>) paper.get("authors");
            if (authors != null && authors.size() > 1) {
                for (int i = 0; i < authors.size(); i++) {
                    for (int j = i + 1; j < authors.size(); j++) {
                        String author1 = authors.get(i);
                        String author2 = authors.get(j);
                        
                        authorCoauthors.computeIfAbsent(author1, k -> new HashSet<>()).add(author2);
                        authorCoauthors.computeIfAbsent(author2, k -> new HashSet<>()).add(author1);
                    }
                }
            }
        }
        
        List<Map<String, Object>> nodes = authorCoauthors.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> node = new HashMap<>();
                    node.put("id", entry.getKey());
                    node.put("label", entry.getKey());
                    node.put("size", entry.getValue().size());
                    return node;
                })
                .collect(Collectors.toList());
        
        List<Map<String, Object>> edges = authorCoauthors.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                        .map(coauthor -> {
                            Map<String, Object> edge = new HashMap<>();
                            edge.put("source", entry.getKey());
                            edge.put("target", coauthor);
                            return edge;
                        }))
                .collect(Collectors.toList());
        
        return Map.of(
                "type", "network",
                "title", "作者合作网络",
                "nodes", nodes,
                "edges", edges
        );
    }
    
    // 辅助方法
    private String extractNodeId(Map<String, Object> node) {
        return (String) node.get("id");
    }
    
    private String extractNodeLabel(Map<String, Object> node) {
        return (String) node.get("name");
    }
    
    private String extractNodeType(Map<String, Object> node) {
        return (String) node.get("type");
    }
    
    private int calculateNodeSize(Map<String, Object> node) {
        Object confidence = node.get("confidence");
        if (confidence instanceof Number) {
            return ((Number) confidence).intValue() * 10 + 10;
        }
        return 20;
    }
    
    private String getNodeColor(String type) {
        switch (type) {
            case "Person": return "#FF6B6B";
            case "Organization": return "#4ECDC4";
            case "Concept": return "#45B7D1";
            case "Method": return "#96CEB4";
            default: return "#FECA57";
        }
    }
    
    private String extractSourceId(Map<String, Object> result) {
        // 根据实际数据结构提取源节点ID
        return "source";
    }
    
    private String extractTargetId(Map<String, Object> result) {
        // 根据实际数据结构提取目标节点ID
        return "target";
    }
    
    private String extractRelationshipType(Map<String, Object> result) {
        // 根据实际数据结构提取关系类型
        return "RELATED_TO";
    }
    
    private String extractYear(String dateString) {
        if (dateString != null && dateString.length() >= 4) {
            return dateString.substring(0, 4);
        }
        return "Unknown";
    }
    
    private Map<String, Object> createEmptyVisualization() {
        Map<String, Object> empty = new HashMap<>();
        empty.put("nodes", new ArrayList<>());
        empty.put("edges", new ArrayList<>());
        empty.put("type", "graph");
        return empty;
    }
}
