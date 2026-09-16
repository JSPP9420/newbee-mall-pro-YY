package com.newbee.mall.service;

import com.newbee.mall.controller.dto.UserLoginDTO;
import com.newbee.mall.controller.dto.UserRegisterDTO;
import com.newbee.mall.controller.vo.LoginVO;
import com.newbee.mall.controller.vo.UserVO;

/**
 * 用户服务接口
 * Controller 只依赖接口，不依赖实现 — 面向接口编程
 */
public interface MallUserService {

    /** 注册 */
    void register(UserRegisterDTO dto);

    /** 登录，返回 token + 用户信息 */
    LoginVO login(UserLoginDTO dto);

    /** 登出，删除 token */
    void logout(String token);

    /** 根据 token 获取当前登录用户 */
    UserVO getCurrentUserByToken(String token);
}
