/**
 * 严肃声明：
 * 开源版本请务必保留此注释头信息，若删除我方将保留所有法律责任追究！
 * 本系统已申请软件著作权，受国家版权局知识产权以及国家计算机软件著作权保护！
 * 可正常分享和学习源码，不得用于违法犯罪活动，违者必究！
 * Copyright (c) 2019-2020 十三 all rights reserved.
 * 版权所有，侵权必究！
 */
package ltd.newbee.mall.service.impl;

import ltd.newbee.mall.common.NewBeeMallCategoryLevelEnum;
import ltd.newbee.mall.common.NewBeeMallException;
import ltd.newbee.mall.common.ServiceResultEnum;
import ltd.newbee.mall.controller.dto.NewBeeMallGoodsSaveDTO;
import ltd.newbee.mall.controller.vo.NewBeeMallGoodsAdminVO;
import ltd.newbee.mall.controller.vo.NewBeeMallGoodsDetailVO;
import ltd.newbee.mall.controller.vo.NewBeeMallSearchGoodsVO;
import ltd.newbee.mall.mapper.GoodsCategoryMapper;
import ltd.newbee.mall.mapper.NewBeeMallGoodsMapper;
import ltd.newbee.mall.entity.GoodsCategory;
import ltd.newbee.mall.entity.NewBeeMallGoods;
import ltd.newbee.mall.service.NewBeeMallGoodsService;
import ltd.newbee.mall.util.BeanUtil;
import ltd.newbee.mall.util.NewBeeMallUtils;
import ltd.newbee.mall.util.PageQueryUtil;
import ltd.newbee.mall.util.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class NewBeeMallGoodsServiceImpl implements NewBeeMallGoodsService {

    private final NewBeeMallGoodsMapper goodsMapper;
    private final GoodsCategoryMapper goodsCategoryMapper;

    public NewBeeMallGoodsServiceImpl(NewBeeMallGoodsMapper goodsMapper,
                                      GoodsCategoryMapper goodsCategoryMapper) {
        this.goodsMapper = goodsMapper;
        this.goodsCategoryMapper = goodsCategoryMapper;
    }

    @Override
    public PageResult getNewBeeMallGoodsPage(PageQueryUtil pageUtil) {
        List<NewBeeMallGoods> goodsList = goodsMapper.findNewBeeMallGoodsList(pageUtil);
        int total = goodsMapper.getTotalNewBeeMallGoods(pageUtil);
        List<NewBeeMallGoodsAdminVO> goodsVOList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(goodsList)) {
            goodsVOList = BeanUtil.copyList(goodsList, NewBeeMallGoodsAdminVO.class);
        }
        return new PageResult(goodsVOList, total, pageUtil.getLimit(), pageUtil.getPage());
    }

    @Override
    public String saveNewBeeMallGoods(NewBeeMallGoodsSaveDTO goodsDTO) {
        GoodsCategory goodsCategory = goodsCategoryMapper.selectByPrimaryKey(goodsDTO.getGoodsCategoryId());
        if (goodsCategory == null || goodsCategory.getCategoryLevel().intValue() != NewBeeMallCategoryLevelEnum.LEVEL_THREE.getLevel()) {
            return ServiceResultEnum.GOODS_CATEGORY_ERROR.getResult();
        }
        if (goodsMapper.selectByCategoryIdAndName(goodsDTO.getGoodsName(), goodsDTO.getGoodsCategoryId()) != null) {
            return ServiceResultEnum.SAME_GOODS_EXIST.getResult();
        }
        NewBeeMallGoods goods = toEntity(goodsDTO);
        if (goodsMapper.insertSelective(goods) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public void batchSaveNewBeeMallGoods(List<NewBeeMallGoods> newBeeMallGoodsList) {
        if (!CollectionUtils.isEmpty(newBeeMallGoodsList)) {
            goodsMapper.batchInsert(newBeeMallGoodsList);
        }
    }

    @Override
    public String updateNewBeeMallGoods(NewBeeMallGoodsSaveDTO goodsDTO) {
        GoodsCategory goodsCategory = goodsCategoryMapper.selectByPrimaryKey(goodsDTO.getGoodsCategoryId());
        if (goodsCategory == null || goodsCategory.getCategoryLevel().intValue() != NewBeeMallCategoryLevelEnum.LEVEL_THREE.getLevel()) {
            return ServiceResultEnum.GOODS_CATEGORY_ERROR.getResult();
        }
        NewBeeMallGoods current = goodsMapper.selectByPrimaryKey(goodsDTO.getGoodsId());
        if (current == null) {
            return ServiceResultEnum.DATA_NOT_EXIST.getResult();
        }
        NewBeeMallGoods sameGoods = goodsMapper.selectByCategoryIdAndName(goodsDTO.getGoodsName(), goodsDTO.getGoodsCategoryId());
        if (sameGoods != null && !sameGoods.getGoodsId().equals(goodsDTO.getGoodsId())) {
            return ServiceResultEnum.SAME_GOODS_EXIST.getResult();
        }
        NewBeeMallGoods goods = toEntity(goodsDTO);
        goods.setUpdateTime(new Date());
        if (goodsMapper.updateByPrimaryKeySelective(goods) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public NewBeeMallGoodsDetailVO getGoodsDetailById(Long id) {
        NewBeeMallGoods goods = getGoods(id);
        if (!Byte.valueOf((byte) 0).equals(goods.getGoodsSellStatus())) {
            NewBeeMallException.fail(ServiceResultEnum.GOODS_PUT_DOWN.getResult());
        }
        NewBeeMallGoodsDetailVO detailVO = new NewBeeMallGoodsDetailVO();
        BeanUtil.copyProperties(goods, detailVO);
        detailVO.setGoodsCarouselList(StringUtils.hasText(goods.getGoodsCarousel())
                ? goods.getGoodsCarousel().split(",") : new String[0]);
        return detailVO;
    }

    @Override
    public NewBeeMallGoodsAdminVO getAdminGoodsById(Long id) {
        NewBeeMallGoodsAdminVO goodsVO = new NewBeeMallGoodsAdminVO();
        BeanUtil.copyProperties(getGoods(id), goodsVO);
        return goodsVO;
    }

    @Override
    public Boolean batchUpdateSellStatus(Long[] ids, int sellStatus) {
        return goodsMapper.batchUpdateSellStatus(ids, sellStatus) > 0;
    }

    @Override
    public PageResult searchNewBeeMallGoods(PageQueryUtil pageUtil) {
        List<NewBeeMallGoods> goodsList = goodsMapper.findNewBeeMallGoodsListBySearch(pageUtil);
        int total = goodsMapper.getTotalNewBeeMallGoodsBySearch(pageUtil);
        List<NewBeeMallSearchGoodsVO> goodsVOList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(goodsList)) {
            goodsVOList = BeanUtil.copyList(goodsList, NewBeeMallSearchGoodsVO.class);
            for (NewBeeMallSearchGoodsVO goodsVO : goodsVOList) {
                if (goodsVO.getGoodsName() != null && goodsVO.getGoodsName().length() > 28) {
                    goodsVO.setGoodsName(goodsVO.getGoodsName().substring(0, 28) + "...");
                }
                if (goodsVO.getGoodsIntro() != null && goodsVO.getGoodsIntro().length() > 30) {
                    goodsVO.setGoodsIntro(goodsVO.getGoodsIntro().substring(0, 30) + "...");
                }
            }
        }
        return new PageResult(goodsVOList, total, pageUtil.getLimit(), pageUtil.getPage());
    }

    private NewBeeMallGoods getGoods(Long id) {
        NewBeeMallGoods goods = goodsMapper.selectByPrimaryKey(id);
        if (goods == null) {
            NewBeeMallException.fail(ServiceResultEnum.GOODS_NOT_EXIST.getResult());
        }
        return goods;
    }

    private NewBeeMallGoods toEntity(NewBeeMallGoodsSaveDTO goodsDTO) {
        NewBeeMallGoods goods = new NewBeeMallGoods();
        goods.setGoodsId(goodsDTO.getGoodsId());
        goods.setGoodsName(NewBeeMallUtils.cleanString(goodsDTO.getGoodsName()));
        goods.setGoodsIntro(NewBeeMallUtils.cleanString(goodsDTO.getGoodsIntro()));
        goods.setGoodsCategoryId(goodsDTO.getGoodsCategoryId());
        goods.setGoodsCoverImg(goodsDTO.getGoodsCoverImg());
        goods.setGoodsCarousel(goodsDTO.getGoodsCarousel());
        goods.setOriginalPrice(goodsDTO.getOriginalPrice());
        goods.setSellingPrice(goodsDTO.getSellingPrice());
        goods.setStockNum(goodsDTO.getStockNum());
        goods.setTag(NewBeeMallUtils.cleanString(goodsDTO.getTag()));
        goods.setGoodsSellStatus(goodsDTO.getGoodsSellStatus() == null ? null : goodsDTO.getGoodsSellStatus().byteValue());
        goods.setGoodsDetailContent(NewBeeMallUtils.cleanString(goodsDTO.getGoodsDetailContent()));
        return goods;
    }
}
