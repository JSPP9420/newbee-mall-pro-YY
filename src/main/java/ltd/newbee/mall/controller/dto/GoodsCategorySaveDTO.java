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
public class GoodsCategorySaveDTO {
    @Positive(message = "分类主键必须为正数")
    private Long categoryId;
    @NotNull(message = "分类层级不能为空")
    @Min(value = 1, message = "分类层级无效")
    @Max(value = 3, message = "分类层级无效")
    private Byte categoryLevel;
    @NotNull(message = "父分类不能为空")
    @PositiveOrZero(message = "父分类无效")
    private Long parentId;
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称长度不能超过50")
    private String categoryName;
    @NotNull(message = "排序值不能为空")
    @PositiveOrZero(message = "排序值不能为负数")
    private Integer categoryRank;
}
