package com.newbee.mall.exception;

import com.newbee.mall.common.ResultCode;
import lombok.Getter;

/**
 * 业务异常
 * Service 层遇到业务问题时直接抛这个异常，不用返回字符串
 * 全局异常处理器会自动接住，转成统一 JSON 返回前端
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }
}
