package yunxun.ai.canary.backend.service.mcp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import yunxun.ai.canary.backend.model.entity.QueryLog;
import yunxun.ai.canary.backend.repository.QueryLogRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 数据库服务工具 - 管理MySQL日志、用户信息和Redis缓存
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DBServiceTool implements MCPTool {
    
    private final QueryLogRepository queryLogRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Override
    public String getToolName() {
        return "db_service_tool";
    }
    
    @Override
    public String getToolDescription() {
        return "管理MySQL日志、用户信息和Redis缓存，提供数据存储和检索服务";
    }
    
    @Override
    public Object execute(Map<String, Object> parameters) {
        try {
            String operation = (String) parameters.get("operation");
            
            switch (operation) {
                case "log_query":
                    return logQuery(parameters);
                case "get_query_history":
                    return getQueryHistory(parameters);
                case "cache_result":
                    return cacheResult(parameters);
                case "get_cached_result":
                    return getCachedResult(parameters);
                case "clear_cache":
                    return clearCache(parameters);
                default:
                    throw new IllegalArgumentException("不支持的操作: " + operation);
            }
            
        } catch (Exception e) {
            log.error("数据库服务工具执行失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }
    
    private Object logQuery(Map<String, Object> parameters) {
        QueryLog queryLog = new QueryLog();
        queryLog.setUserId(Long.valueOf(parameters.get("user_id").toString()));
        queryLog.setQueryText((String) parameters.get("query_text"));
        queryLog.setQueryType((String) parameters.get("query_type"));
        queryLog.setResponse((String) parameters.get("response"));
        queryLog.setExecutionTime(Long.valueOf(parameters.get("execution_time").toString()));
        queryLog.setSuccess((Boolean) parameters.get("success"));
        queryLog.setErrorMessage((String) parameters.get("error_message"));
        
        queryLogRepository.save(queryLog);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "查询日志已保存");
        return result;
    }
    
    private Object getQueryHistory(Map<String, Object> parameters) {
        Long userId = Long.valueOf(parameters.get("user_id").toString());
        List<QueryLog> logs = queryLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("logs", logs);
        result.put("count", logs.size());
        return result;
    }
    
    private Object cacheResult(Map<String, Object> parameters) {
        String key = (String) parameters.get("key");
        Object value = parameters.get("value");
        Integer ttl = (Integer) parameters.getOrDefault("ttl", 3600); // 默认1小时
        
        redisTemplate.opsForValue().set(key, value, ttl, TimeUnit.SECONDS);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "结果已缓存");
        return result;
    }
    
    private Object getCachedResult(Map<String, Object> parameters) {
        String key = (String) parameters.get("key");
        Object value = redisTemplate.opsForValue().get(key);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("cached", value != null);
        result.put("value", value);
        return result;
    }
    
    private Object clearCache(Map<String, Object> parameters) {
        String pattern = (String) parameters.getOrDefault("pattern", "*");
        redisTemplate.delete(redisTemplate.keys(pattern));
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "缓存已清理");
        return result;
    }
    
    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        
        Map<String, Object> properties = new HashMap<>();
        
        Map<String, Object> operationSchema = new HashMap<>();
        operationSchema.put("type", "string");
        operationSchema.put("enum", List.of("log_query", "get_query_history", "cache_result", "get_cached_result", "clear_cache"));
        operationSchema.put("description", "操作类型");
        properties.put("operation", operationSchema);
        
        schema.put("properties", properties);
        schema.put("required", List.of("operation"));
        
        return schema;
    }
}
