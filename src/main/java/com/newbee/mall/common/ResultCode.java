package com.newbee.mall.common;

import lombok.Getter;

/**
 * 错误码枚举
 * 用枚举代替字符串魔法值，打错一个字母编译器就会报错
 * 规范：2xx 成功 / 4xx 客户端错误 / 5xx 服务端错误
 */
@Getter
public enum ResultCode {

    // 通用
    SUCCESS(200, "success"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或认证已过期"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    SERVER_ERROR(500, "服务器内部错误"),

    // 用户模块
    USER_EXISTS(409, "用户名已存在"),
    LOGIN_FAILED(401, "用户名或密码错误"),
    USER_LOCKED(403, "账号已被锁定"),
    TOKEN_INVALID(401, "Token无效或已过期");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
