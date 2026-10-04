/**
 * 严肃声明：
 * 开源版本请务必保留此注释头信息，若删除我方将保留所有法律责任追究！
 * 本系统已申请软件著作权，受国家版权局知识产权以及国家计算机软件著作权保护！
 * 可正常分享和学习源码，不得用于违法犯罪活动，违者必究！
 * Copyright (c) 2019-2020 十三 all rights reserved.
 * 版权所有，侵权必究！
 */
package ltd.newbee.mall.controller.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 购物项 VO（原 NewBeeMallShoppingCartItemVO，pro 版重命名去掉 NewBeeMall 历史前缀）
 * REST 版语义：返回完整商品名，展示裁剪（截断/省略号）由前端负责
 */
@Data
public class ShoppingCartItemVO implements Serializable {

    private Long cartItemId;

    private Long goodsId;

    private Integer goodsCount;

    private String goodsName;

    private String goodsCoverImg;

    private Integer sellingPrice;
}
