package ltd.newbee.mall.controller.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

/**
 * 购物车写入请求参数。
 */
@Data
public class ShoppingCartItemSaveDTO {
    @Positive(message = "购物车项主键必须为正数")
    private Long cartItemId;
    @Positive(message = "商品主键必须为正数")
    private Long goodsId;
    @Positive(message = "商品数量必须为正数")
    private Integer goodsCount;
}
