package yunxun.ai.canary.backend.utils;

import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DataSourceUtils {

    private final MongoTemplate mongoTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final Neo4jClient neo4jClient;
    private final SqlSessionFactory sqlSessionFactory; // MyBatis-Plus

    // === MySQL ===
    public <T> List<T> selectList(Class<T> mapperClass, String method, Object param) throws Exception {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            return session.selectList(mapperClass.getName() + "." + method, param);
        }
    }

    // === MongoDB ===
    public void saveToMongo(String collection, Object data) {
        mongoTemplate.save(data, collection);
    }

    // === Redis ===
    public void setRedisValue(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public Object getRedisValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // === Neo4j ===
    public Collection<Map<String, Object>> queryNeo4j(String cypher) {
        return neo4jClient.query(cypher).fetch().all();
    }
}
