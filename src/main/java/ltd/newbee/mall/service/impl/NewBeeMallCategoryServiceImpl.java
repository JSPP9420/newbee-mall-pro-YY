/**
 * 严肃声明：
 * 开源版本请务必保留此注释头信息，若删除我方将保留所有法律责任追究！
 * 本系统已申请软件著作权，受国家版权局知识产权以及国家计算机软件著作权保护！
 * 可正常分享和学习源码，不得用于违法犯罪活动，违者必究！
 * Copyright (c) 2019-2020 十三 all rights reserved.
 * 版权所有，侵权必究！
 */
package ltd.newbee.mall.service.impl;

import ltd.newbee.mall.common.Constants;
import ltd.newbee.mall.common.NewBeeMallCategoryLevelEnum;
import ltd.newbee.mall.common.ServiceResultEnum;
import ltd.newbee.mall.controller.dto.GoodsCategorySaveDTO;
import ltd.newbee.mall.controller.vo.GoodsCategoryAdminVO;
import ltd.newbee.mall.controller.vo.NewBeeMallIndexCategoryVO;
import ltd.newbee.mall.controller.vo.SearchPageCategoryVO;
import ltd.newbee.mall.controller.vo.SecondLevelCategoryVO;
import ltd.newbee.mall.controller.vo.ThirdLevelCategoryVO;
import ltd.newbee.mall.mapper.GoodsCategoryMapper;
import ltd.newbee.mall.entity.GoodsCategory;
import ltd.newbee.mall.service.NewBeeMallCategoryService;
import ltd.newbee.mall.util.BeanUtil;
import ltd.newbee.mall.util.PageQueryUtil;
import ltd.newbee.mall.util.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service
public class NewBeeMallCategoryServiceImpl implements NewBeeMallCategoryService {

    private final GoodsCategoryMapper goodsCategoryMapper;

    public NewBeeMallCategoryServiceImpl(GoodsCategoryMapper goodsCategoryMapper) {
        this.goodsCategoryMapper = goodsCategoryMapper;
    }

    @Override
    public PageResult getCategorisPage(PageQueryUtil pageUtil) {
        List<GoodsCategory> goodsCategories = goodsCategoryMapper.findGoodsCategoryList(pageUtil);
        int total = goodsCategoryMapper.getTotalGoodsCategories(pageUtil);
        List<GoodsCategoryAdminVO> categoryVOList = CollectionUtils.isEmpty(goodsCategories)
                ? new ArrayList<>() : BeanUtil.copyList(goodsCategories, GoodsCategoryAdminVO.class);
        return new PageResult(categoryVOList, total, pageUtil.getLimit(), pageUtil.getPage());
    }

    @Override
    public String saveCategory(GoodsCategorySaveDTO categoryDTO) {
        GoodsCategory category = toEntity(categoryDTO);
        GoodsCategory temp = goodsCategoryMapper.selectByLevelAndName(category.getCategoryLevel(), category.getCategoryName());
        if (temp != null) {
            return ServiceResultEnum.SAME_CATEGORY_EXIST.getResult();
        }
        if (goodsCategoryMapper.insertSelective(category) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public String updateGoodsCategory(GoodsCategorySaveDTO categoryDTO) {
        GoodsCategory category = toEntity(categoryDTO);
        GoodsCategory temp = goodsCategoryMapper.selectByPrimaryKey(category.getCategoryId());
        if (temp == null) {
            return ServiceResultEnum.DATA_NOT_EXIST.getResult();
        }
        GoodsCategory sameCategory = goodsCategoryMapper.selectByLevelAndName(category.getCategoryLevel(), category.getCategoryName());
        if (sameCategory != null && !sameCategory.getCategoryId().equals(category.getCategoryId())) {
            return ServiceResultEnum.SAME_CATEGORY_EXIST.getResult();
        }
        category.setUpdateTime(new Date());
        if (goodsCategoryMapper.updateByPrimaryKeySelective(category) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public GoodsCategoryAdminVO getAdminCategoryById(Long id) {
        GoodsCategory category = getGoodsCategoryById(id);
        if (category == null) {
            return null;
        }
        GoodsCategoryAdminVO categoryVO = new GoodsCategoryAdminVO();
        BeanUtil.copyProperties(category, categoryVO);
        return categoryVO;
    }

    @Override
    public GoodsCategory getGoodsCategoryById(Long id) {
        return goodsCategoryMapper.selectByPrimaryKey(id);
    }

    @Override
    public Boolean deleteBatch(Integer[] ids) {
        if (ids == null || ids.length < 1) {
            return false;
        }
        return goodsCategoryMapper.deleteBatch(ids) > 0;
    }

    @Override
    public List<NewBeeMallIndexCategoryVO> getCategoriesForIndex() {
        List<NewBeeMallIndexCategoryVO> categoryVOList = new ArrayList<>();
        List<GoodsCategory> firstLevelCategories = goodsCategoryMapper.selectByLevelAndParentIdsAndNumber(
                Collections.singletonList(0L), NewBeeMallCategoryLevelEnum.LEVEL_ONE.getLevel(), Constants.INDEX_CATEGORY_NUMBER);
        if (CollectionUtils.isEmpty(firstLevelCategories)) {
            return null;
        }
        List<Long> firstLevelCategoryIds = firstLevelCategories.stream().map(GoodsCategory::getCategoryId).collect(Collectors.toList());
        List<GoodsCategory> secondLevelCategories = goodsCategoryMapper.selectByLevelAndParentIdsAndNumber(
                firstLevelCategoryIds, NewBeeMallCategoryLevelEnum.LEVEL_TWO.getLevel(), 0);
        if (CollectionUtils.isEmpty(secondLevelCategories)) {
            return categoryVOList;
        }
        List<Long> secondLevelCategoryIds = secondLevelCategories.stream().map(GoodsCategory::getCategoryId).collect(Collectors.toList());
        List<GoodsCategory> thirdLevelCategories = goodsCategoryMapper.selectByLevelAndParentIdsAndNumber(
                secondLevelCategoryIds, NewBeeMallCategoryLevelEnum.LEVEL_THREE.getLevel(), 0);
        if (CollectionUtils.isEmpty(thirdLevelCategories)) {
            return categoryVOList;
        }
        Map<Long, List<GoodsCategory>> thirdLevelCategoryMap = thirdLevelCategories.stream()
                .collect(groupingBy(GoodsCategory::getParentId));
        List<SecondLevelCategoryVO> secondLevelCategoryVOS = new ArrayList<>();
        for (GoodsCategory secondLevelCategory : secondLevelCategories) {
            if (thirdLevelCategoryMap.containsKey(secondLevelCategory.getCategoryId())) {
                SecondLevelCategoryVO secondLevelCategoryVO = new SecondLevelCategoryVO();
                BeanUtil.copyProperties(secondLevelCategory, secondLevelCategoryVO);
                secondLevelCategoryVO.setThirdLevelCategoryVOS(
                        BeanUtil.copyList(thirdLevelCategoryMap.get(secondLevelCategory.getCategoryId()), ThirdLevelCategoryVO.class));
                secondLevelCategoryVOS.add(secondLevelCategoryVO);
            }
        }
        if (CollectionUtils.isEmpty(secondLevelCategoryVOS)) {
            return categoryVOList;
        }
        Map<Long, List<SecondLevelCategoryVO>> secondLevelCategoryVOMap = secondLevelCategoryVOS.stream()
                .collect(groupingBy(SecondLevelCategoryVO::getParentId));
        for (GoodsCategory firstCategory : firstLevelCategories) {
            if (secondLevelCategoryVOMap.containsKey(firstCategory.getCategoryId())) {
                NewBeeMallIndexCategoryVO categoryVO = new NewBeeMallIndexCategoryVO();
                BeanUtil.copyProperties(firstCategory, categoryVO);
                categoryVO.setSecondLevelCategoryVOS(secondLevelCategoryVOMap.get(firstCategory.getCategoryId()));
                categoryVOList.add(categoryVO);
            }
        }
        return categoryVOList;
    }

    @Override
    public SearchPageCategoryVO getCategoriesForSearch(Long categoryId) {
        SearchPageCategoryVO searchPageCategoryVO = new SearchPageCategoryVO();
        GoodsCategory thirdLevelGoodsCategory = goodsCategoryMapper.selectByPrimaryKey(categoryId);
        if (thirdLevelGoodsCategory != null && thirdLevelGoodsCategory.getCategoryLevel() == NewBeeMallCategoryLevelEnum.LEVEL_THREE.getLevel()) {
            GoodsCategory secondLevelGoodsCategory = goodsCategoryMapper.selectByPrimaryKey(thirdLevelGoodsCategory.getParentId());
            if (secondLevelGoodsCategory != null && secondLevelGoodsCategory.getCategoryLevel() == NewBeeMallCategoryLevelEnum.LEVEL_TWO.getLevel()) {
                List<GoodsCategory> thirdLevelCategories = goodsCategoryMapper.selectByLevelAndParentIdsAndNumber(
                        Collections.singletonList(secondLevelGoodsCategory.getCategoryId()), NewBeeMallCategoryLevelEnum.LEVEL_THREE.getLevel(), Constants.SEARCH_CATEGORY_NUMBER);
                searchPageCategoryVO.setCurrentCategoryName(thirdLevelGoodsCategory.getCategoryName());
                searchPageCategoryVO.setSecondLevelCategoryName(secondLevelGoodsCategory.getCategoryName());
                searchPageCategoryVO.setThirdLevelCategoryList(thirdLevelCategories);
                return searchPageCategoryVO;
            }
        }
        return null;
    }

    @Override
    public List<GoodsCategory> selectByLevelAndParentIdsAndNumber(List<Long> parentIds, int categoryLevel) {
        return goodsCategoryMapper.selectByLevelAndParentIdsAndNumber(parentIds, categoryLevel, 0);
    }

    private GoodsCategory toEntity(GoodsCategorySaveDTO categoryDTO) {
        GoodsCategory category = new GoodsCategory();
        category.setCategoryId(categoryDTO.getCategoryId());
        category.setCategoryLevel(categoryDTO.getCategoryLevel());
        category.setParentId(categoryDTO.getParentId());
        category.setCategoryName(categoryDTO.getCategoryName());
        category.setCategoryRank(categoryDTO.getCategoryRank());
        return category;
    }
}
