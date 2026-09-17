package ltd.newbee.mall.controller.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MallUserUpdateDTO {
    @Size(max = 50, message = "昵称长度不能超过50")
    private String nickName;
    @Size(max = 100, message = "个性签名长度不能超过100")
    private String introduceSign;
    @Size(max = 150, message = "收货地址长度不能超过150")
    private String address;
}
