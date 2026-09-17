package ltd.newbee.mall.controller.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
@Data
public class IndexConfigAdminVO implements Serializable {
    private Long configId;
    private String configName;
    private Byte configType;
    private Long goodsId;
    private String redirectUrl;
    private Integer configRank;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
