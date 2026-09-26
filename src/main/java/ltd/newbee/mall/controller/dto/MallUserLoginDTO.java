package ltd.newbee.mall.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户端登录/注册请求 DTO（pro 版新增）
 * 登录与注册入参字段相同（loginName + password），共用一个 DTO
 * 对比页面版：MallUserController 用 3 个散装 @RequestParam 手写判空（verifyCode 除外）；
 * REST 版用 DTO + JSR303 注解，校验失败由 NewBeeMallExceptionHandler 统一返回 400 JSON
 *
 * 验证码：页面版验证码存在 Session 里，纯 Token API 下不适用，REST 版暂不做（与管理端同决策）
 */
@Data
public class MallUserLoginDTO {

    @NotBlank(message = "请输入登录名")
    private String loginName;

    @NotBlank(message = "请输入密码")
    private String password;
}
