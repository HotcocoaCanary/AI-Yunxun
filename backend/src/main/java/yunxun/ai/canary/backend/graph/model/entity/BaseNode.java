package yunxun.ai.canary.backend.graph.model.entity;

import lombok.*;

import java.util.HashMap;
import java.util.Map;

// 通用节点基类
@Data
public abstract class BaseNode {
    protected String id;
    protected String label;  // 节点标签，如 "User", "Product"
    protected Map<String, Object> properties = new HashMap<>(); // 属性键值对

    public BaseNode(String label) {
        this.label = label;
    }

    public void addProperty(String key, Object value) {
        properties.put(key, value);
    }
}
