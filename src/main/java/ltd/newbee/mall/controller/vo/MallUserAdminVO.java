package ltd.newbee.mall.controller.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
@Data
public class MallUserAdminVO implements Serializable {
    private Long userId;
    private String nickName;
    private String loginName;
    private String introduceSign;
    private String address;
    private Byte isDeleted;
    private Byte lockedFlag;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
