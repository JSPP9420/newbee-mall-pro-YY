package ltd.newbee.mall.entity;

import lombok.Data;

import java.util.Date;

/**
 * 用户端登录 Token（pro 版新增）
 * 与 AdminToken 同套路：登录态不存 Session，
 * Token 返回给前端，每次请求放在 Authorization: Bearer <token> 头里带来
 * 区别：user_id 是 Long（用户表主键 bigint），管理端 admin_user_id 是 Integer
 */
@Data
public class MallUserToken {
    private Long userTokenId;

    private String token;

    private Long userId;

    private Date expiresAt;

    private Date createTime;
}
