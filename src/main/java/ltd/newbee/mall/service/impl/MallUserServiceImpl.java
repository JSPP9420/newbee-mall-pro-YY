/**
 * 严肃声明：
 * 开源版本请务必保留此注释头信息，若删除我方将保留所有法律责任追究！
 * 本系统已申请软件著作权，受国家版权局知识产权以及国家计算机软件著作权保护！
 * 可正常分享和学习源码，不得用于违法犯罪活动，违者必究！
 * Copyright (c) 2019-2020 十三 all rights reserved.
 * 版权所有，侵权必究！
 */
package ltd.newbee.mall.service.impl;

import ltd.newbee.mall.common.NewBeeMallException;
import ltd.newbee.mall.common.ServiceResultEnum;
import ltd.newbee.mall.controller.dto.MallUserUpdateDTO;
import ltd.newbee.mall.controller.vo.MallUserAdminVO;
import ltd.newbee.mall.controller.vo.MallUserVO;
import ltd.newbee.mall.mapper.MallUserMapper;
import ltd.newbee.mall.entity.MallUser;
import ltd.newbee.mall.service.MallUserService;
import ltd.newbee.mall.util.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class MallUserServiceImpl implements MallUserService {

    private final MallUserMapper mallUserMapper;
    private final PasswordEncoder passwordEncoder;

    public MallUserServiceImpl(MallUserMapper mallUserMapper,
                                      PasswordEncoder passwordEncoder) {
        this.mallUserMapper = mallUserMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public PageResult getNewBeeMallUsersPage(PageQueryUtil pageUtil) {
        List<MallUser> mallUsers = mallUserMapper.findMallUserList(pageUtil);
        int total = mallUserMapper.getTotalMallUsers(pageUtil);
        List<MallUserAdminVO> userAdminVOs = BeanUtil.copyList(mallUsers, MallUserAdminVO.class);
        return new PageResult(userAdminVOs, total, pageUtil.getLimit(), pageUtil.getPage());
    }

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
     * 登录（统一契约：失败必抛异常）
     * 注意失败消息的顺序设计：
     * - 用户不存在与密码错误统一提示"用户名或密码错误"（防账号枚举）
     * - 锁定检查放在密码校验之后——密码不对时不能暴露"该账号存在且被锁定"
     */
    @Override
    public MallUserVO login(String loginName, String password) {
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

    @Override
    public MallUserVO updateUserInfo(Long userId, MallUserUpdateDTO updateDTO) {
        MallUser userFromDB = mallUserMapper.selectByPrimaryKey(userId);
        if (userFromDB != null) {
            if (StringUtils.hasText(updateDTO.getNickName())) {
                userFromDB.setNickName(NewBeeMallUtils.cleanString(updateDTO.getNickName()));
            }
            if (StringUtils.hasText(updateDTO.getAddress())) {
                userFromDB.setAddress(NewBeeMallUtils.cleanString(updateDTO.getAddress()));
            }
            if (StringUtils.hasText(updateDTO.getIntroduceSign())) {
                userFromDB.setIntroduceSign(NewBeeMallUtils.cleanString(updateDTO.getIntroduceSign()));
            }
            if (mallUserMapper.updateByPrimaryKeySelective(userFromDB) > 0) {
                MallUserVO newBeeMallUserVO = new MallUserVO();
                BeanUtil.copyProperties(userFromDB, newBeeMallUserVO);
                return newBeeMallUserVO;
            }
        }
        return null;
    }

    @Override
    public Boolean lockUsers(Integer[] ids, int lockStatus) {
        if (ids.length < 1) {
            return false;
        }
        return mallUserMapper.lockUserBatch(ids, lockStatus) > 0;
    }
}
