package ltd.newbee.mall.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 后台订单修改请求参数。
 */
@Data
public class OrderUpdateDTO {
    @NotNull(message = "订单主键不能为空")
    @Positive(message = "订单主键必须为正数")
    private Long orderId;
    @NotNull(message = "订单金额不能为空")
    @Positive(message = "订单金额必须为正数")
    private Integer totalPrice;
    @NotBlank(message = "收货地址不能为空")
    @Size(max = 150, message = "收货地址长度不能超过150")
    private String userAddress;
}
