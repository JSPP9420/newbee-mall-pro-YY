package com.newbee.mall.controller.vo;

import lombok.Data;

/**
 * 用户信息 VO（返回给前端）
 * 对比原项目：绝不包含 password 字段
 */
@Data
public class UserVO {

    private Long userId;
    private String loginName;
    private String nickName;
    private String introduceSign;
    private Integer status;
}
