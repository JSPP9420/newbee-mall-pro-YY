package ltd.newbee.mall.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户端注册请求 DTO（pro 版新增，安全加固 #6 从 MallUserLoginDTO 拆出）
 *
 * 为什么登录/注册不能共用一个 DTO：
 * 注册要约束密码强度（@Size(min=6)），登录不能加——否则存量弱密码用户
 * 连登录都被 400 拦截（参数校验发生在进 Service 之前，轮不到比对密码）。
 * 校验规则属于各自业务场景，拆开各自声明。
 */
@Data
public class MallUserRegisterDTO {

    @NotBlank(message = "请输入登录名")
    @Pattern(regexp = "^[a-zA-Z0-9_]{4,20}$", message = "登录名需4-20位字母、数字或下划线")
    private String loginName;

    @NotBlank(message = "请输入密码")
    @Size(min = 6, max = 32, message = "密码长度需6-32位")
    private String password;
}
