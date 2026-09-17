package ltd.newbee.mall.entity;

import lombok.Data;

@Data
public class AdminPermission {
    private Long permissionId;
    private String permissionCode;
    private String permissionName;
    private String httpMethod;
    private String requestPath;
    private Boolean enabled;
}
