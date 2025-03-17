package com.example.aiyunxun.service.impl;

import com.example.aiyunxun.repository.Neo4jOperator;
import com.example.aiyunxun.util.EntityExtractor;
import com.example.aiyunxun.service.NLPService;
import com.example.aiyunxun.util.EntityToCypherQuery;
import com.example.aiyunxun.util.NormalizedData;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class NLPServiceImpl implements NLPService {

    @Resource
    private EntityExtractor entityExtractor;

    @Resource
    private Neo4jOperator neo4jOperator;

    @Override
    public ObjectNode answer(String question) {
        Map<String, Object> entity = entityExtractor.extractEntityAttributes(question);
        List<String> cypherQuery = new EntityToCypherQuery().getCypherQuery(entity);
        List<Map<String, Object>> data = new ArrayList<>();
        return new NormalizedData().normalizedCypher(data);
    }
}
