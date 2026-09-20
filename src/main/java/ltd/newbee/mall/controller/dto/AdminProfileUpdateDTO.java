package ltd.newbee.mall.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改管理员昵称/登录名 DTO（pro 版新增）
 *
 * 对比原项目 AdminController.nameUpdate()：用 @RequestParam 收两个散参数 + 手写 if 判空；
 * REST 版用 DTO + JSR303 注解
 */
@Data
public class AdminProfileUpdateDTO {

    @NotBlank(message = "登录名不能为空")
    @Size(max = 20, message = "登录名最多20个字符")
    private String loginUserName;

    @NotBlank(message = "昵称不能为空")
    @Size(max = 20, message = "昵称最多20个字符")
    private String nickName;
}
