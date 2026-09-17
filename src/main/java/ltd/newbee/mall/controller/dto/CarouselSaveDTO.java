package ltd.newbee.mall.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CarouselSaveDTO {
    @Positive(message = "轮播图主键必须为正数")
    private Integer carouselId;

    @NotBlank(message = "轮播图地址不能为空")
    @Size(max = 100, message = "轮播图地址长度不能超过100")
    private String carouselUrl;

    @Size(max = 100, message = "跳转地址长度不能超过100")
    private String redirectUrl;

    @NotNull(message = "排序值不能为空")
    private Integer carouselRank;

}
