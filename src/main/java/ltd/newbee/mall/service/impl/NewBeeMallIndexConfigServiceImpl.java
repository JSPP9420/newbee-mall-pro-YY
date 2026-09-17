/**
 * 严肃声明：
 * 开源版本请务必保留此注释头信息，若删除我方将保留所有法律责任追究！
 * 本系统已申请软件著作权，受国家版权局知识产权以及国家计算机软件著作权保护！
 * 可正常分享和学习源码，不得用于违法犯罪活动，违者必究！
 * Copyright (c) 2019-2020 十三 all rights reserved.
 * 版权所有，侵权必究！
 */
package ltd.newbee.mall.service.impl;

import ltd.newbee.mall.common.ServiceResultEnum;
import ltd.newbee.mall.controller.dto.IndexConfigSaveDTO;
import ltd.newbee.mall.controller.vo.IndexConfigAdminVO;
import ltd.newbee.mall.controller.vo.NewBeeMallIndexConfigGoodsVO;
import ltd.newbee.mall.mapper.IndexConfigMapper;
import ltd.newbee.mall.mapper.NewBeeMallGoodsMapper;
import ltd.newbee.mall.entity.IndexConfig;
import ltd.newbee.mall.entity.NewBeeMallGoods;
import ltd.newbee.mall.service.NewBeeMallIndexConfigService;
import ltd.newbee.mall.util.BeanUtil;
import ltd.newbee.mall.util.PageQueryUtil;
import ltd.newbee.mall.util.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NewBeeMallIndexConfigServiceImpl implements NewBeeMallIndexConfigService {

    private final IndexConfigMapper indexConfigMapper;
    private final NewBeeMallGoodsMapper goodsMapper;

    public NewBeeMallIndexConfigServiceImpl(IndexConfigMapper indexConfigMapper,
                                            NewBeeMallGoodsMapper goodsMapper) {
        this.indexConfigMapper = indexConfigMapper;
        this.goodsMapper = goodsMapper;
    }

    @Override
    public PageResult getConfigsPage(PageQueryUtil pageUtil) {
        List<IndexConfig> indexConfigs = indexConfigMapper.findIndexConfigList(pageUtil);
        int total = indexConfigMapper.getTotalIndexConfigs(pageUtil);
        List<IndexConfigAdminVO> configVOList = CollectionUtils.isEmpty(indexConfigs)
                ? new ArrayList<>() : BeanUtil.copyList(indexConfigs, IndexConfigAdminVO.class);
        return new PageResult(configVOList, total, pageUtil.getLimit(), pageUtil.getPage());
    }

    @Override
    public String saveIndexConfig(IndexConfigSaveDTO configDTO) {
        IndexConfig indexConfig = toEntity(configDTO);
        if (goodsMapper.selectByPrimaryKey(indexConfig.getGoodsId()) == null) {
            return ServiceResultEnum.GOODS_NOT_EXIST.getResult();
        }
        if (indexConfigMapper.selectByTypeAndGoodsId(indexConfig.getConfigType(), indexConfig.getGoodsId()) != null) {
            return ServiceResultEnum.SAME_INDEX_CONFIG_EXIST.getResult();
        }
        if (indexConfigMapper.insertSelective(indexConfig) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public String updateIndexConfig(IndexConfigSaveDTO configDTO) {
        IndexConfig indexConfig = toEntity(configDTO);
        if (goodsMapper.selectByPrimaryKey(indexConfig.getGoodsId()) == null) {
            return ServiceResultEnum.GOODS_NOT_EXIST.getResult();
        }
        IndexConfig current = indexConfigMapper.selectByPrimaryKey(indexConfig.getConfigId());
        if (current == null) {
            return ServiceResultEnum.DATA_NOT_EXIST.getResult();
        }
        IndexConfig sameConfig = indexConfigMapper.selectByTypeAndGoodsId(indexConfig.getConfigType(), indexConfig.getGoodsId());
        if (sameConfig != null && !sameConfig.getConfigId().equals(indexConfig.getConfigId())) {
            return ServiceResultEnum.SAME_INDEX_CONFIG_EXIST.getResult();
        }
        indexConfig.setUpdateTime(new Date());
        if (indexConfigMapper.updateByPrimaryKeySelective(indexConfig) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public IndexConfigAdminVO getAdminIndexConfigById(Long id) {
        IndexConfig config = getIndexConfigById(id);
        if (config == null) {
            return null;
        }
        IndexConfigAdminVO configVO = new IndexConfigAdminVO();
        BeanUtil.copyProperties(config, configVO);
        return configVO;
    }

    @Override
    public IndexConfig getIndexConfigById(Long id) {
        return indexConfigMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<NewBeeMallIndexConfigGoodsVO> getConfigGoodsesForIndex(int configType, int number) {
        List<NewBeeMallIndexConfigGoodsVO> goodsVOList = new ArrayList<>(number);
        List<IndexConfig> indexConfigs = indexConfigMapper.findIndexConfigsByTypeAndNum(configType, number);
        if (!CollectionUtils.isEmpty(indexConfigs)) {
            List<Long> goodsIds = indexConfigs.stream().map(IndexConfig::getGoodsId).collect(Collectors.toList());
            List<NewBeeMallGoods> goodsList = goodsMapper.selectByPrimaryKeys(goodsIds);
            goodsVOList = BeanUtil.copyList(goodsList, NewBeeMallIndexConfigGoodsVO.class);
            for (NewBeeMallIndexConfigGoodsVO goodsVO : goodsVOList) {
                if (goodsVO.getGoodsName() != null && goodsVO.getGoodsName().length() > 30) {
                    goodsVO.setGoodsName(goodsVO.getGoodsName().substring(0, 30) + "...");
                }
                if (goodsVO.getGoodsIntro() != null && goodsVO.getGoodsIntro().length() > 22) {
                    goodsVO.setGoodsIntro(goodsVO.getGoodsIntro().substring(0, 22) + "...");
                }
            }
        }
        return goodsVOList;
    }

    @Override
    public Boolean deleteBatch(Long[] ids) {
        if (ids == null || ids.length < 1) {
            return false;
        }
        return indexConfigMapper.deleteBatch(ids) > 0;
    }

    private IndexConfig toEntity(IndexConfigSaveDTO configDTO) {
        IndexConfig config = new IndexConfig();
        config.setConfigId(configDTO.getConfigId());
        config.setConfigName(configDTO.getConfigName());
        config.setConfigType(configDTO.getConfigType());
        config.setGoodsId(configDTO.getGoodsId());
        config.setRedirectUrl(configDTO.getRedirectUrl());
        config.setConfigRank(configDTO.getConfigRank());
        return config;
    }
}
