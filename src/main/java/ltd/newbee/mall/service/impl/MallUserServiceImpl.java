/**
 * 严肃声明：
 * 开源版本请务必保留此注释头信息，若删除我方将保留所有法律责任追究！
 * 本系统已申请软件著作权，受国家版权局知识产权以及国家计算机软件著作权保护！
 * 可正常分享和学习源码，不得用于违法犯罪活动，违者必究！
 * Copyright (c) 2019-2020 十三 all rights reserved.
 * 版权所有，侵权必究！
 */
package ltd.newbee.mall.service.impl;

import lombok.RequiredArgsConstructor;
import ltd.newbee.mall.common.NewBeeMallException;
import ltd.newbee.mall.common.ServiceResultEnum;
import ltd.newbee.mall.controller.dto.MallUserLoginDTO;
import ltd.newbee.mall.controller.dto.MallUserUpdateDTO;
import ltd.newbee.mall.controller.vo.MallUserVO;
import ltd.newbee.mall.mapper.MallUserMapper;
import ltd.newbee.mall.mapper.MallUserTokenMapper;
import ltd.newbee.mall.entity.MallUser;
import ltd.newbee.mall.entity.MallUserToken;
import ltd.newbee.mall.service.MallUserService;
import ltd.newbee.mall.util.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MallUserServiceImpl implements MallUserService {

    /** Token 有效期：7 天（与管理端 AdminServiceImpl 保持一致） */
    private static final int TOKEN_EXPIRE_HOURS = 24 * 7;

    private final MallUserMapper mallUserMapper;
    private final MallUserTokenMapper mallUserTokenMapper;
    private final PasswordEncoder passwordEncoder;


    @Override
    public void register(String loginName, String password) {
        if (mallUserMapper.selectByLoginName(loginName) != null) {
            throw new NewBeeMallException("该登录名已存在");
        }
        MallUser registerUser = new MallUser();
        registerUser.setLoginName(loginName);
        registerUser.setNickName(loginName);
        registerUser.setLoginPassword(passwordEncoder.encode(password));
        if (mallUserMapper.insertSelective(registerUser) <= 0) {
            throw new NewBeeMallException("注册失败，请稍后重试");
        }
    }

    /**
     * 登录校验（统一契约：失败必抛异常）
     * 页面版 Controller 移除后仅 loginAndIssueToken() 内部复用，故下沉为私有方法
     *
     * 注意失败消息的顺序设计：
     * - 用户不存在与密码错误统一提示"用户名或密码错误"（防账号枚举）
     * - 锁定检查放在密码校验之后——密码不对时不能暴露"该账号存在且被锁定"
     */
    private MallUserVO login(String loginName, String password) {
        MallUser user = mallUserMapper.selectByLoginName(loginName);
        if (user == null) {
            throw new NewBeeMallException("用户名或密码错误");
        }
        String storedPassword = user.getLoginPassword();
        if (storedPassword == null || !passwordEncoder.matches(password, storedPassword)) {
            throw new NewBeeMallException("用户名或密码错误");
        }
        if (user.getLockedFlag() == 1) {
            throw new NewBeeMallException(ServiceResultEnum.LOGIN_USER_LOCKED.getResult());
        }

        if (user.getNickName() != null && user.getNickName().length() > 7) {
            user.setNickName(user.getNickName().substring(0, 7) + "..");
        }
        MallUserVO userVO = new MallUserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * pro 版新增：REST 登录，校验通过后签发 Token
     * 复用 login() 的统一契约（失败必抛异常），失败场景不用再写一遍
     */
    @Override
    public String loginAndIssueToken(MallUserLoginDTO dto) {
        MallUserVO loginUser = login(dto.getLoginName(), dto.getPassword());

        // 签发 Token：UUID 去横线，入库并设置 7 天过期（与管理端同套路）
        String token = UUID.randomUUID().toString().replace("-", "");
        MallUserToken userToken = new MallUserToken();
        userToken.setToken(token);
        userToken.setUserId(loginUser.getUserId());
        userToken.setExpiresAt(new Date(System.currentTimeMillis() + TOKEN_EXPIRE_HOURS * 3600 * 1000L));
        userToken.setCreateTime(new Date());
        mallUserTokenMapper.insert(userToken);

        return token;
    }

    @Override
    public void logoutByToken(String token) {
        mallUserTokenMapper.deleteByToken(token);
    }

    @Override
    public MallUserVO getUserById(Long userId) {
        MallUser user = mallUserMapper.selectByPrimaryKey(userId);
        if (user == null) {
            throw new NewBeeMallException("用户不存在");
        }
        if (user.getNickName() != null && user.getNickName().length() > 7) {
            user.setNickName(user.getNickName().substring(0, 7) + "..");
        }
        MallUserVO userVO = new MallUserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * 修改用户资料（统一契约：失败必抛异常，异常消息即失败原因）
     * 仅更新传入的非空字段（昵称/收货地址/签名），成功返回更新后的用户信息
     */
    @Override
    public MallUserVO updateUserInfo(Long userId, MallUserUpdateDTO updateDTO) {
        MallUser userFromDB = mallUserMapper.selectByPrimaryKey(userId);
        if (userFromDB == null) {
            throw new NewBeeMallException("用户不存在");
        }
        if (StringUtils.hasText(updateDTO.getNickName())) {
            userFromDB.setNickName(NewBeeMallUtils.cleanString(updateDTO.getNickName()));
        }
        if (StringUtils.hasText(updateDTO.getAddress())) {
            userFromDB.setAddress(NewBeeMallUtils.cleanString(updateDTO.getAddress()));
        }
        if (StringUtils.hasText(updateDTO.getIntroduceSign())) {
            userFromDB.setIntroduceSign(NewBeeMallUtils.cleanString(updateDTO.getIntroduceSign()));
        }
        if (mallUserMapper.updateByPrimaryKeySelective(userFromDB) <= 0) {
            throw new NewBeeMallException("修改失败，请稍后重试");
        }
        MallUserVO userVO = new MallUserVO();
        BeanUtil.copyProperties(userFromDB, userVO);
        return userVO;
    }
}
