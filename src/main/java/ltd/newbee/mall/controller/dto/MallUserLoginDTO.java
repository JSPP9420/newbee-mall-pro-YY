package ltd.newbee.mall.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户端登录请求 DTO（pro 版新增）
 * 只做非空校验，不加密码强度——存量弱密码用户必须还能登录，
 * 注册的强度约束在 MallUserRegisterDTO（安全加固 #6 拆分）
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
