package com.example.aiyunxun.controller;

import com.example.aiyunxun.common.Response;
import com.example.aiyunxun.service.NLPService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@Controller
public class NLPController {

    @Resource
    private NLPService NLPService;

    @PostMapping("/answer")
    public Response<ObjectNode> answer(@RequestBody Map<String, String> requestBody) {
        String question = requestBody.get("question");
        ObjectNode answer = NLPService.answer(question);
        return Response.success(answer);
    }
}
