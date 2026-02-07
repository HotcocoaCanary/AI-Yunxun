package com.example.cloudhuntchartbackend.utils;

import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: 实体转换查询语句
 * @Author: Canary
 * @Date: 2024/9/7 下午6:22
 */

@Configuration
public class EntityToCypherQuery {

    public List<String> getCypherQuery(Map<String, Object> entity) {
        if (entity == null || entity.isEmpty()) {
            return Collections.emptyList();
        }

        return entity.entrySet().stream()
                .map(entry -> buildQuery(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private String buildQuery(String key, Object value) {
        StringBuilder query = new StringBuilder("MATCH p=(n)-[r]->(m) WHERE ");

        switch (key) {
            case "Author":
            case "Institution":
                query.append("(n.").append("name").append("='").append(value).append("' OR m.")
                        .append("name").append("='").append(value).append("') ");
                break;
            case "Date":
                query.append("(n.").append(key).append("='").append(value).append("' OR m.")
                        .append(key).append("='").append(value).append("') ");
                break;
            default:
                throw new IllegalArgumentException("Unsupported key: " + key);
        }

        query.append("RETURN p");
        return query.toString();
    }
}
