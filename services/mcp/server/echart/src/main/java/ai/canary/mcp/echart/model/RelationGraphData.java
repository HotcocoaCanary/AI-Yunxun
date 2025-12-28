package ai.canary.mcp.echart.model;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class RelationGraphData {
    private String title;
    private List<Node> nodes;
    private List<Link> links;
    
    @Data
    public static class Node {
        private String id;
        private String name;
        private String category;
        private Number value;
        private Map<String, Object> properties;
    }
    
    @Data
    public static class Link {
        private String source;
        private String target;
        private String name;
        private Number value;
        private Map<String, Object> properties;
    }
}

