package com.example.aiyunxun.controller;

import com.example.aiyunxun.common.Response;
import com.example.aiyunxun.service.AdminService;
import com.example.aiyunxun.util.JwtUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AdminController {

    @Resource
    private AdminService adminService;

    @Value("${password}")
    private String password;

    @Resource
    private JwtUtil jwtUtil;


    @PostMapping("/accredit")
    public Response<String> accredit(@RequestBody Map<String, String> requestBody) {
        String password = requestBody.get("password");
        String token = adminService.accredit(password);
        if (token != null) {
            return Response.success(token);
        }
        return Response.error("少年，你还没有觉醒呢");
    }
}
