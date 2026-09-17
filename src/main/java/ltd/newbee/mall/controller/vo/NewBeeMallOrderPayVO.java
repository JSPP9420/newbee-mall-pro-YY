package ltd.newbee.mall.controller.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 支付页面所需订单信息。
 */
@Data
public class NewBeeMallOrderPayVO implements Serializable {
    private String orderNo;
    private Integer totalPrice;

}
