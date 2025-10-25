package com.example.aiyunxun.controller;

import com.example.aiyunxun.common.Response;
import com.example.aiyunxun.service.Neo4jService;
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

    @PostMapping("/all")
    public Response<ObjectNode> getAll(@RequestBody Map<String, String> requestBody) {
        int limit = Integer.parseInt(requestBody.get("limit"));
        ObjectNode result = neo4jService.getAll(limit);
        return Response.success(result);
    }

    @PostMapping("/update")
    public Response<ObjectNode> search() {
        try {
            neo4jService.update();
            return Response.success();
        } catch (Exception e) {
            return Response.error("请上传符合要求的Excel文件");
        }
    }
}