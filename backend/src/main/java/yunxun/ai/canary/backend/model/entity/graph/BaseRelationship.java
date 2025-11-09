package yunxun.ai.canary.backend.model.entity.graph;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

// 通用关系基类
@Data
public abstract class BaseRelationship {
    protected String id;
    protected String label; // 关系标签，如 "use"
    protected BaseNode startNode;
    protected BaseNode endNode;
    protected Map<String, Object> properties = new HashMap<>();

    public BaseRelationship(String label, BaseNode startNode, BaseNode endNode) {
        this.label = label;
        this.startNode = startNode;
        this.endNode = endNode;
    }

    public void addProperty(String key, Object value) {
        properties.put(key, value);
    }
}
