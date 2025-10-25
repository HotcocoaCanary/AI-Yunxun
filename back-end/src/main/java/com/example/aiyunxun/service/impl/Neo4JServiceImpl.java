package com.example.aiyunxun.service.impl;

import com.example.aiyunxun.repository.Neo4jOperator;
import com.example.aiyunxun.service.Neo4jService;
import com.example.aiyunxun.util.NormalizedData;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class Neo4JServiceImpl implements Neo4jService {

    @Resource
    private Neo4jOperator neo4jOperator;

    @Override
    public ObjectNode getAll(int limit) {
        String cypherQuery = "MATCH p=()-[]->() RETURN p";
        if (limit > 0) {
            cypherQuery += " LIMIT " + limit;
        }
        List<Map<String, Object>> data = neo4jOperator.executeCypher(cypherQuery);
        for (Map<String, Object> map :data){
            System.out.println(map);
        }
        return new NormalizedData().normalizedCypher(data);
    }

    @Override
    public void update() {

    }
}
