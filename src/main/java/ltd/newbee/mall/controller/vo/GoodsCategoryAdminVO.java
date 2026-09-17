package ltd.newbee.mall.controller.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
@Data
public class GoodsCategoryAdminVO implements Serializable {
    private Long categoryId;
    private Byte categoryLevel;
    private Long parentId;
    private String categoryName;
    private Integer categoryRank;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
