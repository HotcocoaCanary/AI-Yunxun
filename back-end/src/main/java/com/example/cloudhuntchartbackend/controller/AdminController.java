package com.example.cloudhuntchartbackend.controller;

import com.example.cloudhuntchartbackend.member.Result;
import com.example.cloudhuntchartbackend.utils.JwtUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Canary
 * @version 1.0.0
 * @title AdminController
 * @description <TODO description class purpose>
 * @creat 2024/10/11 上午7:03
 **/
@RestController
public class AdminController {

    @Value("${password}")
    private String password;

    @Resource
    private JwtUtil jwtUtil;


    @PostMapping("/accredit")
    public Result<String> accredit(@RequestBody Map<String, String> requestBody) {
        String password = requestBody.get("password");

        if (this.password.equals(password)) {
            Map<String, Object> claims = new HashMap<>();
            claims.put("message", "少年，你该觉醒成为管理员啦");
            String token = jwtUtil.getToken(claims);
            return Result.success(token);
        }else {
            return Result.error();
        }

    }
}
