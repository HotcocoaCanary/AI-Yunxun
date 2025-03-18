package com.example.aiyunxun.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 统一响应结果
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Response<T> {
    private Integer code; // 业务状态码
    private String message; // 提示信息
    private T data; // 响应数据

    // 快速返回操作成功响应结果(带响应数据)
    public static <T> Response<T> success(T data) {
        return new Response<>(StatusCode.SUCCESS.getCode(), StatusCode.SUCCESS.getMessage(), data);
    }

    // 快速返回操作成功响应结果
    public static <T> Response<T> success() {
        return success(null);
    }

    // 快速返回操作失败响应结果
    public static <T> Response<T> error(String message) {
        return new Response<>(StatusCode.ERROR.getCode(), message, null);
    }

    // 快速返回操作失败响应结果，使用默认错误消息
    public static <T> Response<T> error() {
        return error(StatusCode.ERROR.getMessage());
    }
}
