package ltd.newbee.mall.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改密码 DTO（pro 版新增）
 *
 * 对比原项目 AdminController.passwordUpdate()：用 @RequestParam 收两个散参数 + 手写 if 判空；
 * REST 版用 DTO + JSR303 注解，校验失败统一返回 400 JSON
 *
 * 为什么有 confirmPassword？
 * 原项目页面版没有确认密码字段（页面有 JS 校验），
 * REST 版前端可能不做校验，后端兜底更安全
 */
@Data
public class AdminPasswordUpdateDTO {

    @NotBlank(message = "原密码不能为空")
    private String originalPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度需6-32位")
    private String newPassword;

    /**
     * 确认(的)密码
     */
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
}
