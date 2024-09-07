package com.example.cloudhuntchartbackend.controller;

import com.example.cloudhuntchartbackend.member.Result;
import com.example.cloudhuntchartbackend.service.EntityExtractorService;
import com.example.cloudhuntchartbackend.service.Neo4jService;
import com.example.cloudhuntchartbackend.utils.EntityToCypherQuery;
import com.example.cloudhuntchartbackend.utils.NormalizedData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class Neo4jController {
    @Resource
    private EntityToCypherQuery entityToCypherQuery;

    @Resource
    private Neo4jService neo4jService;

    @Resource
    private EntityExtractorService entityExtractorService;

    @Resource
    private NormalizedData normalizedData;

    @PostMapping("/answer")
    public Result<ObjectNode> answer(@RequestBody Map<String, String> requestBody) {
        try {
            String answer = requestBody.get("answer");
            Map<String, Object> entity = entityExtractorService.extractEntityAttributes(answer);
            List<String> cypherQuery = entityToCypherQuery.getCypherQuery(entity);
            List<Map<String, Object>> data = neo4jService.executeCypherQueries(cypherQuery);
            ObjectNode result = normalizedData.normalizedCypher(data);
            return Result.success(result);
        } catch (JsonProcessingException e) {
            return Result.error("查询错误" + e.getMessage());
        }
    }
}