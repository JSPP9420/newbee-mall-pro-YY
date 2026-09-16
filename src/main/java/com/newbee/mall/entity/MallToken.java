package com.newbee.mall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录 Token 实体 — 与 mall_token 表一一对应
 * M1a 用数据库存 Token；M1b 会替换成 Redis
 */
@Data
@TableName("mall_token")
public class MallToken {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String token;

    private Long userId;

    private LocalDateTime expiresAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
