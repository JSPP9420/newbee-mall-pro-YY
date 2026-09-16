package com.newbee.mall.controller.vo;

import lombok.Data;

/**
 * 登录成功返回 VO
 */
@Data
public class LoginVO {

    private String token;
    private UserVO user;

    public static LoginVO of(String token, UserVO user) {
        LoginVO vo = new LoginVO();
        vo.token = token;
        vo.user = user;
        return vo;
    }
}
