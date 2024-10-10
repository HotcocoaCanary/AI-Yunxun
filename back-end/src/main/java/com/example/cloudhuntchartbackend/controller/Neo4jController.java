package com.example.cloudhuntchartbackend.controller;

import com.example.cloudhuntchartbackend.member.Result;
import com.example.cloudhuntchartbackend.service.Neo4jService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class Neo4jController {

    @Resource
    private Neo4jService neo4jService;

    @PostMapping("/answer")
    public Result<ObjectNode> answer(@RequestBody Map<String, String> requestBody) {
        String answer = requestBody.get("answer");
        ObjectNode result = neo4jService.getAnswer(answer);
        return Result.success(result);
    }

    @PostMapping("/all")
    public Result<ObjectNode> getAll() {
        int limit = 300;
        ObjectNode result = neo4jService.findAll(limit);
        return Result.success(result);
    }
}