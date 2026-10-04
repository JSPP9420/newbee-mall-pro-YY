package ltd.newbee.mall.service;

import ltd.newbee.mall.controller.dto.ShoppingCartItemSaveDTO;
import ltd.newbee.mall.controller.vo.CartVO;
import ltd.newbee.mall.controller.vo.ShoppingCartItemVO;

import java.util.List;

public interface NewBeeMallShoppingCartService {

    /**
     * 添加购物项（统一契约：失败必抛异常，异常消息即失败原因）
     * 校验规则：商品存在且上架、库存充足、单件数量 1-5、购物车总项数不超上限
     * 若该商品已在购物车中则等价于修改数量
     *
     * @param userId   当前登录用户 ID
     * @param cartItem 待添加的购物项（goodsId + goodsCount）
     */
    void saveNewBeeMallCartItem(Long userId, ShoppingCartItemSaveDTO cartItem);

    /**
     * 修改购物项数量（统一契约：失败必抛异常）
     * 校验规则：购物项存在且归属当前用户、商品上架、库存充足、数量 1-5
     *
     * @param userId   当前登录用户 ID
     * @param cartItem 待修改的购物项（cartItemId + goodsCount）
     */
    void updateNewBeeMallCartItem(Long userId, ShoppingCartItemSaveDTO cartItem);

    /**
     * 删除购物项（统一契约：失败必抛异常）
     *
     * @param shoppingCartItemId 待删除的购物项 ID
     * @param userId             当前登录用户 ID（校验归属，防越权删除他人购物项）
     */
    void deleteById(Long shoppingCartItemId, Long userId);

    /**
     * pro 版新增：REST 查询当前用户购物车聚合数据
     * 页面版把 itemsTotal/priceTotal 计算写在 Controller 里，REST 版将计算下沉到 Service
     *
     * @param userId 当前登录用户 ID
     * @return 购物车聚合视图（明细 + 总件数 + 总价）
     */
    CartVO getMyCart(Long userId);

    /**
     * 获取当前用户购物车中的列表数据。
     * 注意：OrderController（订单结算流程）仍在使用，页面版移除后保留至订单模块 REST 化。
     */
    List<ShoppingCartItemVO> getMyShoppingCartItems(Long userId);
}
