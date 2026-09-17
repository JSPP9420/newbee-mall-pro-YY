package ltd.newbee.mall.entity;

import lombok.Data;

import java.util.Date;

/**
 * 管理员登录 Token（pro 版新增）
 * 与 Session 版的区别：登录态不再存在服务端 Session 里，
 * 而是生成一个 Token 返回给前端，前端每次请求放在 Authorization 头里带来
 */
@Data
public class AdminToken {
    private Integer adminTokenId;

    private String token;

    private Integer adminUserId;

    private Date expiresAt;

    private Date createTime;
}
