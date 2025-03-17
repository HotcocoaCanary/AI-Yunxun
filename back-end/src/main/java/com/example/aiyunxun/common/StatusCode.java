package com.example.aiyunxun.common;

import lombok.Getter;

// 枚举定义状态码和消息
@Getter
public enum StatusCode {
    SUCCESS(200, "操作成功"),
    ERROR(400, "操作失败"),
    NO_PERMISSION(403, "无权限");

    private final Integer code;
    private final String message;

    StatusCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
