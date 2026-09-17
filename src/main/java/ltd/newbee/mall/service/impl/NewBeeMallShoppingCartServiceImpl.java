package ltd.newbee.mall.service.impl;

import ltd.newbee.mall.common.Constants;
import ltd.newbee.mall.common.ServiceResultEnum;
import ltd.newbee.mall.controller.dto.ShoppingCartItemSaveDTO;
import ltd.newbee.mall.controller.vo.NewBeeMallShoppingCartItemVO;
import ltd.newbee.mall.mapper.NewBeeMallGoodsMapper;
import ltd.newbee.mall.mapper.NewBeeMallShoppingCartItemMapper;
import ltd.newbee.mall.entity.NewBeeMallGoods;
import ltd.newbee.mall.entity.NewBeeMallShoppingCartItem;
import ltd.newbee.mall.service.NewBeeMallShoppingCartService;
import ltd.newbee.mall.util.BeanUtil;
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
public class NewBeeMallShoppingCartServiceImpl implements NewBeeMallShoppingCartService {

    private final NewBeeMallShoppingCartItemMapper newBeeMallShoppingCartItemMapper;
    private final NewBeeMallGoodsMapper newBeeMallGoodsMapper;

    public NewBeeMallShoppingCartServiceImpl(
            NewBeeMallShoppingCartItemMapper newBeeMallShoppingCartItemMapper,
            NewBeeMallGoodsMapper newBeeMallGoodsMapper) {
        this.newBeeMallShoppingCartItemMapper = newBeeMallShoppingCartItemMapper;
        this.newBeeMallGoodsMapper = newBeeMallGoodsMapper;
    }

    @Override
    public String saveNewBeeMallCartItem(Long userId, ShoppingCartItemSaveDTO cartItem) {
        if (userId == null || cartItem == null || cartItem.getGoodsId() == null
                || !isValidCount(cartItem.getGoodsCount())) {
            return ServiceResultEnum.SHOPPING_ITEM_ERROR.getResult();
        }
        NewBeeMallGoods goods = newBeeMallGoodsMapper.selectByPrimaryKey(cartItem.getGoodsId());
        String goodsCheckResult = validateGoods(goods, cartItem.getGoodsCount());
        if (goodsCheckResult != null) {
            return goodsCheckResult;
        }
        NewBeeMallShoppingCartItem existing =
                newBeeMallShoppingCartItemMapper.selectByUserIdAndGoodsId(userId, cartItem.getGoodsId());
        if (existing != null) {
            existing.setGoodsCount(cartItem.getGoodsCount());
            return updateNewBeeMallCartItem(userId, toDTO(existing));
        }
        int totalItem = newBeeMallShoppingCartItemMapper.selectCountByUserId(userId) + 1;
        if (totalItem > Constants.SHOPPING_CART_ITEM_TOTAL_NUMBER) {
            return ServiceResultEnum.SHOPPING_CART_ITEM_TOTAL_NUMBER_ERROR.getResult();
        }
        NewBeeMallShoppingCartItem entity = new NewBeeMallShoppingCartItem();
        entity.setUserId(userId);
        entity.setGoodsId(cartItem.getGoodsId());
        entity.setGoodsCount(cartItem.getGoodsCount());
        entity.setIsDeleted((byte) 0);
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        if (newBeeMallShoppingCartItemMapper.insertSelective(entity) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public String updateNewBeeMallCartItem(Long userId, ShoppingCartItemSaveDTO cartItem) {
        if (userId == null || cartItem == null || cartItem.getCartItemId() == null
                || !isValidCount(cartItem.getGoodsCount())) {
            return ServiceResultEnum.SHOPPING_ITEM_ERROR.getResult();
        }
        NewBeeMallShoppingCartItem current =
                newBeeMallShoppingCartItemMapper.selectByIdAndUserId(cartItem.getCartItemId(), userId);
        if (current == null) {
            return ServiceResultEnum.DATA_NOT_EXIST.getResult();
        }
        NewBeeMallGoods goods = newBeeMallGoodsMapper.selectByPrimaryKey(current.getGoodsId());
        String goodsCheckResult = validateGoods(goods, cartItem.getGoodsCount());
        if (goodsCheckResult != null) {
            return goodsCheckResult;
        }
        if (cartItem.getGoodsCount().equals(current.getGoodsCount())) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        current.setGoodsCount(cartItem.getGoodsCount());
        current.setUpdateTime(new Date());
        if (newBeeMallShoppingCartItemMapper.updateByPrimaryKeySelective(current) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public Boolean deleteById(Long shoppingCartItemId, Long userId) {
        if (shoppingCartItemId == null || userId == null) {
            return false;
        }
        return newBeeMallShoppingCartItemMapper.deleteByPrimaryKey(shoppingCartItemId, userId) > 0;
    }

    @Override
    public List<NewBeeMallShoppingCartItemVO> getMyShoppingCartItems(Long newBeeMallUserId) {
        List<NewBeeMallShoppingCartItemVO> cartItemVOs = new ArrayList<>();
        if (newBeeMallUserId == null) {
            return cartItemVOs;
        }
        List<NewBeeMallShoppingCartItem> cartItems = newBeeMallShoppingCartItemMapper.selectByUserId(
                newBeeMallUserId, Constants.SHOPPING_CART_ITEM_TOTAL_NUMBER);
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
            NewBeeMallShoppingCartItemVO cartItemVO = new NewBeeMallShoppingCartItemVO();
            BeanUtil.copyProperties(cartItem, cartItemVO);
            cartItemVO.setGoodsCoverImg(goods.getGoodsCoverImg());
            String goodsName = goods.getGoodsName();
            cartItemVO.setGoodsName(goodsName != null && goodsName.length() > 28
                    ? goodsName.substring(0, 28) + "..." : goodsName);
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

    private String validateGoods(NewBeeMallGoods goods, Integer goodsCount) {
        if (goods == null) {
            return ServiceResultEnum.GOODS_NOT_EXIST.getResult();
        }
        if (!Byte.valueOf((byte) Constants.SELL_STATUS_UP).equals(goods.getGoodsSellStatus())) {
            return ServiceResultEnum.GOODS_PUT_DOWN.getResult();
        }
        if (goods.getStockNum() == null || goods.getStockNum() < goodsCount) {
            return ServiceResultEnum.SHOPPING_ITEM_COUNT_ERROR.getResult();
        }
        return null;
    }

    private ShoppingCartItemSaveDTO toDTO(NewBeeMallShoppingCartItem entity) {
        ShoppingCartItemSaveDTO dto = new ShoppingCartItemSaveDTO();
        dto.setCartItemId(entity.getCartItemId());
        dto.setGoodsId(entity.getGoodsId());
        dto.setGoodsCount(entity.getGoodsCount());
        return dto;
    }
}
