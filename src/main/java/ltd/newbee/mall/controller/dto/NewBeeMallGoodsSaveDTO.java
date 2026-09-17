package ltd.newbee.mall.controller.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewBeeMallGoodsSaveDTO {
    @Positive(message = "商品主键必须为正数")
    private Long goodsId;
    @NotBlank(message = "商品名称不能为空")
    @Size(max = 200, message = "商品名称长度不能超过200")
    private String goodsName;
    @NotBlank(message = "商品简介不能为空")
    @Size(max = 200, message = "商品简介长度不能超过200")
    private String goodsIntro;
    @NotNull(message = "商品分类不能为空")
    @PositiveOrZero(message = "商品分类无效")
    private Long goodsCategoryId;
    @NotBlank(message = "商品封面不能为空")
    @Size(max = 200, message = "商品封面地址长度不能超过200")
    private String goodsCoverImg;
    @Size(max = 500, message = "商品轮播图地址长度不能超过500")
    private String goodsCarousel;
    @NotNull(message = "原价不能为空")
    @Positive(message = "原价必须为正数")
    private Integer originalPrice;
    @NotNull(message = "售价不能为空")
    @Positive(message = "售价必须为正数")
    private Integer sellingPrice;
    @NotNull(message = "库存不能为空")
    @PositiveOrZero(message = "库存不能为负数")
    private Integer stockNum;
    @NotBlank(message = "商品标签不能为空")
    @Size(max = 20, message = "商品标签长度不能超过20")
    private String tag;
    @NotNull(message = "销售状态不能为空")
    @Min(value = 0, message = "销售状态无效")
    @Max(value = 1, message = "销售状态无效")
    private Integer goodsSellStatus;
    @NotBlank(message = "商品详情不能为空")
    private String goodsDetailContent;

}
