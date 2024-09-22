package com.example.cloudhuntchartbackend;

import com.example.cloudhuntchartbackend.service.EntityExtractorService;
import com.example.cloudhuntchartbackend.service.Neo4jService;
import com.example.cloudhuntchartbackend.utils.EntityToCypherQuery;
import com.example.cloudhuntchartbackend.utils.NormalizedData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class Neo4jTest {

    @Resource
    private EntityToCypherQuery entityToCypherQuery;

    @Resource
    private Neo4jService neo4jService;

    @Resource
    private EntityExtractorService entityExtractorService;

    @Resource
    private NormalizedData normalizedData;

    @Test
    public void testExtractEntities() throws JsonProcessingException {
        List<String> query = new ArrayList<>();
        query.add("MATCH p=()-->() RETURN p LIMIT 25");
        List<Map<String, Object>> maps = neo4jService.executeCypherQueries(query);
        System.out.println(normalizedData.normalizedCypher(maps));
    }

    @Test
    public void test2() throws JsonProcessingException {
        String answer = "What are the papers written by Varrelmann";
        Map<String, Object> entity = entityExtractorService.extractEntityAttributes(answer);
        List<String> cypherQuery = entityToCypherQuery.getCypherQuery(entity);
        List<Map<String, Object>> data = neo4jService.executeCypherQueries(cypherQuery);
        System.out.println(normalizedData.normalizedCypher(data));
    }
}
