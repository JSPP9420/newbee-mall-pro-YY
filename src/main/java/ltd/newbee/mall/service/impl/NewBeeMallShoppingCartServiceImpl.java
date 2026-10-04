package ltd.newbee.mall.service.impl;

import lombok.RequiredArgsConstructor;
import ltd.newbee.mall.common.Constants;
import ltd.newbee.mall.common.NewBeeMallException;
import ltd.newbee.mall.common.ServiceResultEnum;
import ltd.newbee.mall.controller.dto.ShoppingCartItemSaveDTO;
import ltd.newbee.mall.controller.vo.CartVO;
import ltd.newbee.mall.controller.vo.ShoppingCartItemVO;
import ltd.newbee.mall.mapper.NewBeeMallGoodsMapper;
import ltd.newbee.mall.mapper.NewBeeMallShoppingCartItemMapper;
import ltd.newbee.mall.entity.NewBeeMallGoods;
import ltd.newbee.mall.entity.NewBeeMallShoppingCartItem;
import ltd.newbee.mall.service.NewBeeMallShoppingCartService;
import ltd.newbee.mall.util.BeanUtil;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewBeeMallShoppingCartServiceImpl implements NewBeeMallShoppingCartService {

    private final NewBeeMallShoppingCartItemMapper newBeeMallShoppingCartItemMapper;
    private final NewBeeMallGoodsMapper newBeeMallGoodsMapper;

    @Override
    public void saveNewBeeMallCartItem(Long userId, ShoppingCartItemSaveDTO shoppingCartItemSaveDTO) {
        if (userId == null || shoppingCartItemSaveDTO == null || shoppingCartItemSaveDTO.getGoodsId() == null
                || !isValidCount(shoppingCartItemSaveDTO.getGoodsCount())) {
            throw new NewBeeMallException(ServiceResultEnum.SHOPPING_ITEM_ERROR.getResult());
        }
        NewBeeMallGoods goods = newBeeMallGoodsMapper.selectByPrimaryKey(shoppingCartItemSaveDTO.getGoodsId());

        validateGoods(goods, shoppingCartItemSaveDTO.getGoodsCount());

        NewBeeMallShoppingCartItem existing =
                newBeeMallShoppingCartItemMapper.selectByUserIdAndGoodsId(userId, shoppingCartItemSaveDTO.getGoodsId());
        if (existing != null) {
            // 购物车已有该商品：等价于修改数量
            existing.setGoodsCount(shoppingCartItemSaveDTO.getGoodsCount());
            updateNewBeeMallCartItem(userId, toDTO(existing));
            return;
        }
        // 购物车无该商品：默认变为1，和京东购物差不多道理
        int totalItem = newBeeMallShoppingCartItemMapper.selectCountByUserId(userId) + 1;
        if (totalItem > Constants.SHOPPING_CART_ITEM_TOTAL_NUMBER) {
            throw new NewBeeMallException(ServiceResultEnum.SHOPPING_CART_ITEM_TOTAL_NUMBER_ERROR.getResult());
        }
        NewBeeMallShoppingCartItem entity = new NewBeeMallShoppingCartItem();
        entity.setUserId(userId);
        entity.setGoodsId(shoppingCartItemSaveDTO.getGoodsId());
        entity.setGoodsCount(shoppingCartItemSaveDTO.getGoodsCount());
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());

        try {
            if (newBeeMallShoppingCartItemMapper.insertSelective(entity) <= 0) {
                throw new NewBeeMallException(ServiceResultEnum.DB_ERROR.getResult());
            }
        } catch (DuplicateKeyException e) {
            // 并发兜底（V6）：上面的 selectByUserIdAndGoodsId 检查与 insert 之间存在竞态窗口，
            // 另一并发请求可能恰好先插入了同 (user_id, goods_id) 行，insert 撞 uk_user_goods 唯一索引。
            // 唯一索引是数据库层的最后防线，比任何应用层 check-then-act 都可靠；
            // 捕获冲突后转为修改已存在行，保证并发加购收敛到一行
            NewBeeMallShoppingCartItem winner =
                    newBeeMallShoppingCartItemMapper.selectByUserIdAndGoodsId(userId, shoppingCartItemSaveDTO.getGoodsId());
            if (winner == null) {
                throw new NewBeeMallException(ServiceResultEnum.DB_ERROR.getResult());
            }
            winner.setGoodsCount(shoppingCartItemSaveDTO.getGoodsCount());
            //TODO这里商品数量从两个变到一个了吗？怎么还要修改【updateNewBeeMallCartItem方法】，难道存在幂等性问题？留着自己思考（提示：幂等短路）
            updateNewBeeMallCartItem(userId, toDTO(winner));
        }
    }

    @Override
    public void updateNewBeeMallCartItem(Long userId, ShoppingCartItemSaveDTO shoppingCartItemSaveDTO) {
        if (userId == null || shoppingCartItemSaveDTO == null || shoppingCartItemSaveDTO.getCartItemId() == null
                || !isValidCount(shoppingCartItemSaveDTO.getGoodsCount())) {
            throw new NewBeeMallException(ServiceResultEnum.SHOPPING_ITEM_ERROR.getResult());
        }
        NewBeeMallShoppingCartItem current =
                newBeeMallShoppingCartItemMapper.selectByIdAndUserId(shoppingCartItemSaveDTO.getCartItemId(), userId);
        if (current == null) {
            throw new NewBeeMallException(ServiceResultEnum.DATA_NOT_EXIST.getResult());
        }
        NewBeeMallGoods goods = newBeeMallGoodsMapper.selectByPrimaryKey(current.getGoodsId());

        validateGoods(goods, shoppingCartItemSaveDTO.getGoodsCount());

        // 数量未变化时视为成功（幂等），不必产生一次无效 UPDATE，即幂等短路
        if (shoppingCartItemSaveDTO.getGoodsCount().equals(current.getGoodsCount())) {
            return;
        }
        current.setGoodsCount(shoppingCartItemSaveDTO.getGoodsCount());
        current.setUpdateTime(new Date());
        if (newBeeMallShoppingCartItemMapper.updateByPrimaryKeySelective(current) <= 0) {
            throw new NewBeeMallException(ServiceResultEnum.DB_ERROR.getResult());
        }
    }

    @Override
    public void deleteById(Long shoppingCartItemId, Long userId) {
        if (shoppingCartItemId == null || userId == null) {
            throw new NewBeeMallException(ServiceResultEnum.SHOPPING_ITEM_ERROR.getResult());
        }
        // SQL 带 userId 条件，天然防止越权删除他人购物项
        if (newBeeMallShoppingCartItemMapper.deleteByPrimaryKey(shoppingCartItemId, userId) <= 0) {
            throw new NewBeeMallException(ServiceResultEnum.DATA_NOT_EXIST.getResult());
        }
    }

    @Override
    public CartVO getMyCart(Long userId) {
        CartVO cartVO = new CartVO();
        List<ShoppingCartItemVO> items = getMyShoppingCartItems(userId);
        cartVO.setItems(items);
        cartVO.setItemsTotal(items.stream().mapToInt(ShoppingCartItemVO::getGoodsCount).sum());
        cartVO.setPriceTotal(items.stream()
                .mapToInt(item -> item.getGoodsCount() * item.getSellingPrice())
                .sum());
        return cartVO;
    }

    @Override
    public List<ShoppingCartItemVO> getMyShoppingCartItems(Long userId) {
        List<ShoppingCartItemVO> cartItemVOs = new ArrayList<>();
        if (userId == null) {
            return cartItemVOs;
        }
        List<NewBeeMallShoppingCartItem> cartItems = newBeeMallShoppingCartItemMapper.selectByUserId(userId);
        if (CollectionUtils.isEmpty(cartItems)) {
            return cartItemVOs;
        }

        List<Long> goodsIds = cartItems.stream()
                .map(NewBeeMallShoppingCartItem::getGoodsId)
                .collect(Collectors.toList());
        List<NewBeeMallGoods> goodsList = newBeeMallGoodsMapper.selectByPrimaryKeys(goodsIds);

        Map<Long, NewBeeMallGoods> goodsMap = CollectionUtils.isEmpty(goodsList)
                ? new HashMap<>()
                : goodsList.stream().collect(Collectors.toMap(
                        NewBeeMallGoods::getGoodsId, Function.identity(), (first, second) -> first));
        for (NewBeeMallShoppingCartItem cartItem : cartItems) {
            NewBeeMallGoods goods = goodsMap.get(cartItem.getGoodsId());
            if (goods == null) {
                continue;
            }
            ShoppingCartItemVO cartItemVO = new ShoppingCartItemVO();
            BeanUtil.copyProperties(cartItem, cartItemVO);
            // REST 版返回完整商品名，展示裁剪（截断/省略号）由前端负责
            cartItemVO.setGoodsName(goods.getGoodsName());
            cartItemVO.setGoodsCoverImg(goods.getGoodsCoverImg());
            cartItemVO.setSellingPrice(goods.getSellingPrice());
            cartItemVOs.add(cartItemVO);
        }
        return cartItemVOs;
    }

    private boolean isValidCount(Integer goodsCount) {
        return goodsCount != null
                && goodsCount >= 1
                && goodsCount <= Constants.SHOPPING_CART_ITEM_LIMIT_NUMBER;
    }

    /**
     * 商品可购校验（统一契约：校验不过必抛异常）
     */
    private void validateGoods(NewBeeMallGoods goods, Integer goodsCount) {
        if (goods == null) {
            throw new NewBeeMallException(ServiceResultEnum.GOODS_NOT_EXIST.getResult());
        }
        if (!Byte.valueOf((byte) Constants.SELL_STATUS_UP).equals(goods.getGoodsSellStatus())) {
            throw new NewBeeMallException(ServiceResultEnum.GOODS_PUT_DOWN.getResult());
        }
        if (goods.getStockNum() == null || goods.getStockNum() < goodsCount) {
            throw new NewBeeMallException(ServiceResultEnum.SHOPPING_ITEM_COUNT_ERROR.getResult());
        }
    }

    private ShoppingCartItemSaveDTO toDTO(NewBeeMallShoppingCartItem entity) {
        ShoppingCartItemSaveDTO shoppingCartItemSaveDTO = new ShoppingCartItemSaveDTO();
        shoppingCartItemSaveDTO.setCartItemId(entity.getCartItemId());
        shoppingCartItemSaveDTO.setGoodsId(entity.getGoodsId());
        shoppingCartItemSaveDTO.setGoodsCount(entity.getGoodsCount());
        return shoppingCartItemSaveDTO;
    }
}
