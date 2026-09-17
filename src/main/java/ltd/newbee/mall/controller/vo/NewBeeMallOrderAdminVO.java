package ltd.newbee.mall.controller.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 后台订单列表 VO。
 */
@Data
public class NewBeeMallOrderAdminVO implements Serializable {
    private Long orderId;
    private String orderNo;
    private Long userId;
    private Integer totalPrice;
    private Byte payStatus;
    private Byte payType;
    private Date payTime;
    private Byte orderStatus;
    private String extraInfo;
    private String userAddress;
    private Byte isDeleted;
    private Date createTime;
    private Date updateTime;

}
