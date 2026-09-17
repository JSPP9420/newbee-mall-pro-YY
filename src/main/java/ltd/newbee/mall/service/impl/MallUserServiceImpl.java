/**
 * 严肃声明：
 * 开源版本请务必保留此注释头信息，若删除我方将保留所有法律责任追究！
 * 本系统已申请软件著作权，受国家版权局知识产权以及国家计算机软件著作权保护！
 * 可正常分享和学习源码，不得用于违法犯罪活动，违者必究！
 * Copyright (c) 2019-2020 十三 all rights reserved.
 * 版权所有，侵权必究！
 */
package ltd.newbee.mall.service.impl;

import ltd.newbee.mall.common.ServiceResultEnum;
import ltd.newbee.mall.controller.dto.MallUserUpdateDTO;
import ltd.newbee.mall.controller.vo.MallUserAdminVO;
import ltd.newbee.mall.controller.vo.MallUserVO;
import ltd.newbee.mall.mapper.MallUserMapper;
import ltd.newbee.mall.entity.MallUser;
import ltd.newbee.mall.service.MallUserService;
import ltd.newbee.mall.service.dto.UserLoginResult;
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
    public String register(String loginName, String password) {
        if (mallUserMapper.selectByLoginName(loginName) != null) {
            return ServiceResultEnum.SAME_LOGIN_NAME_EXIST.getResult();
        }
        MallUser registerUser = new MallUser();
        registerUser.setLoginName(loginName);
        registerUser.setNickName(loginName);
        String passwordHash = passwordEncoder.encode(password);
        registerUser.setPasswordMd5(passwordHash);
        if (mallUserMapper.insertSelective(registerUser) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public UserLoginResult login(String loginName, String password) {
        MallUser user = mallUserMapper.selectByLoginNameForLogin(loginName);
        if (user == null) {
            return UserLoginResult.failure(ServiceResultEnum.LOGIN_ERROR.getResult());
        }
        if (user.getLockedFlag() == 1) {
            return UserLoginResult.failure(ServiceResultEnum.LOGIN_USER_LOCKED.getResult());
        }

        String storedPassword = user.getPasswordMd5();
        boolean passwordMatches = storedPassword != null
                && (passwordEncoder.matches(password, storedPassword)
                || MD5Util.MD5Encode(password, "UTF-8").equalsIgnoreCase(storedPassword));
        if (!passwordMatches) {
            return UserLoginResult.failure(ServiceResultEnum.LOGIN_ERROR.getResult());
        }
        if (storedPassword.length() == 32) {
            mallUserMapper.updatePasswordHash(user.getUserId(), passwordEncoder.encode(password));
        }

        if (user.getNickName() != null && user.getNickName().length() > 7) {
            user.setNickName(user.getNickName().substring(0, 7) + "..");
        }
        MallUserVO userVO = new MallUserVO();
        BeanUtil.copyProperties(user, userVO);
        return UserLoginResult.success(userVO);
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
