package com.example.aiyunxun.service.impl;

import com.example.aiyunxun.service.AdminService;
import com.example.aiyunxun.util.JwtUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AdminServiceImpl implements AdminService {

    @Value("${password}")
    private String password;

    @Resource
    private JwtUtil jwtUtil;

    @Override
    public String accredit(String password) {
        if (this.password.equals(password)) {
            Map<String, Object> claims = new HashMap<>();
            claims.put("message", "少年，你该觉醒成为管理员啦");
            return jwtUtil.getToken(claims);
        } else {
            return null;
        }
    }
}
