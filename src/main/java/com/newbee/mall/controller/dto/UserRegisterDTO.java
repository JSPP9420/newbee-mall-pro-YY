package com.newbee.mall.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求 DTO
 * JSR303 注解让参数校验自动完成，Controller 不用手写 if 判空
 */
@Data
public class UserRegisterDTO {

    @NotBlank(message = "登录名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9]{4,20}$", message = "登录名需4-20位字母或数字")
    private String loginName;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 32, message = "密码长度需8-32位")
    private String password;
}
