package com.newbee.mall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体 — 与 mall_user 表一一对应
 * 注意：Entity 不直接返回给前端，要通过 VO 转换
 */
@Data
@TableName("mall_user")
public class MallUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String loginName;

    private String password;

    private String nickName;

    private String introduceSign;

    /** 0正常 1锁定 */
    private Integer status;

    /** 逻辑删除标识：0正常 1已删除（MP 自动处理） */
    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
