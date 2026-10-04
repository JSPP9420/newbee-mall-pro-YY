package ltd.newbee.mall.controller.vo;

import lombok.Data;

import java.util.List;

/**
 * 购物车聚合视图（REST 出参）
 * 页面版在 Controller 里拼 itemsTotal/priceTotal，REST 版将聚合计算收敛到 Service，
 * 前端一次请求即可渲染完整购物车
 */
@Data
public class CartVO {

    /** 购物项总件数（各明细数量之和，用于导航角标展示） */
    private int itemsTotal;

    /** 购物车总价（各明细 数量*售价 之和，单位：元） */
    private int priceTotal;

    /** 购物车明细列表 */
    private List<ShoppingCartItemVO> items;
}
