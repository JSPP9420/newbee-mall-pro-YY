package ltd.newbee.mall.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理端登录请求 DTO（pro 版新增）
 * 对比原项目：AdminController.login() 用 3 个散装 @RequestParam 手写判空；
 * REST 版用 DTO + JSR303 注解，校验失败由 NewBeeMallExceptionHandler 统一返回 400 JSON
 */
@Data
public class AdminUserLoginDTO {

    @NotBlank(message = "请输入登录名")
    private String userName;

    @NotBlank(message = "请输入密码")
    private String password;
}
