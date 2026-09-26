/**
 * 严肃声明：
 * 开源版本请务必保留此注释头信息，若删除我方将保留所有法律责任追究！
 * 本系统已申请软件著作权，受国家版权局知识产权以及国家计算机软件著作权保护！
 * 可正常分享和学习源码，不得用于违法犯罪活动，违者必究！
 * Copyright (c) 2019-2020 十三 all rights reserved.
 * 版权所有，侵权必究！
 */
package ltd.newbee.mall.service;

import ltd.newbee.mall.controller.dto.MallUserUpdateDTO;
import ltd.newbee.mall.controller.vo.MallUserVO;
import ltd.newbee.mall.util.PageQueryUtil;
import ltd.newbee.mall.util.PageResult;

public interface MallUserService {
    /**
     * 后台分页
     *
     * @param pageUtil
     * @return
     */
    PageResult getNewBeeMallUsersPage(PageQueryUtil pageUtil);

    /**
     * 用户注册（统一契约：失败必抛异常，异常消息即失败原因）
     *
     * @param loginName
     * @param password
     */
    void register(String loginName, String password);

    /**
     * 登录（统一契约：失败必抛异常；成功返回用户信息）
     *
     * @param loginName
     * @param password 明文密码
     * @return 登录成功的用户信息（不含密码）
     */
    MallUserVO login(String loginName, String password);

    /**
     * 用户信息修改并返回最新的用户信息
     *
     * @param mallUser
     * @return
     */
    MallUserVO updateUserInfo(Long userId, MallUserUpdateDTO updateDTO);

    /**
     * 用户禁用与解除禁用(0-未锁定 1-已锁定)
     *
     * @param ids
     * @param lockStatus
     * @return
     */
    Boolean lockUsers(Integer[] ids, int lockStatus);
}
