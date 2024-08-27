package com.example.cloudhuntchartbackend.member;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 统一响应结果
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Result<T> {
    private Integer code; // 业务状态码
    private String message; // 提示信息
    private T data; // 响应数据

    // 枚举定义状态码和消息
    @Getter
    public enum Status {
        SUCCESS(0, "操作成功"),
        ERROR(1, "操作失败");

        private final Integer code;
        private final String message;

        Status(Integer code, String message) {
            this.code = code;
            this.message = message;
        }

    }

    // 快速返回操作成功响应结果(带响应数据)
    public static <T> Result<T> success(T data) {
        return new Result<>(Status.SUCCESS.getCode(), Status.SUCCESS.getMessage(), data);
    }

    // 快速返回操作成功响应结果
    public static <T> Result<T> success() {
        return success(null);
    }

    // 快速返回操作失败响应结果
    public static <T> Result<T> error(String message) {
        return new Result<>(Status.ERROR.getCode(), message, null);
    }

    // 快速返回操作失败响应结果，使用默认错误消息
    public static <T> Result<T> error() {
        return error(Status.ERROR.getMessage());
    }
}
