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
public class IndexConfigSaveDTO {
    @Positive(message = "配置主键必须为正数")
    private Long configId;
    @NotBlank(message = "配置名称不能为空")
    @Size(max = 50, message = "配置名称长度不能超过50")
    private String configName;
    @NotNull(message = "配置类型不能为空")
    @Min(value = 1, message = "配置类型无效")
    @Max(value = 5, message = "配置类型无效")
    private Byte configType;
    @Positive(message = "商品主键必须为正数")
    private Long goodsId;
    @Size(max = 100, message = "跳转地址长度不能超过100")
    private String redirectUrl;
    @NotNull(message = "排序值不能为空")
    @PositiveOrZero(message = "排序值不能为负数")
    private Integer configRank;
}
