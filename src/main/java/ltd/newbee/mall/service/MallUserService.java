/**
 * 严肃声明：
 * 开源版本请务必保留此注释头信息，若删除我方将保留所有法律责任追究！
 * 本系统已申请软件著作权，受国家版权局知识产权以及国家计算机软件著作权保护！
 * 可正常分享和学习源码，不得用于违法犯罪活动，违者必究！
 * Copyright (c) 2019-2020 十三 all rights reserved.
 * 版权所有，侵权必究！
 */
package ltd.newbee.mall.service;

import ltd.newbee.mall.controller.dto.MallUserLoginDTO;
import ltd.newbee.mall.controller.dto.MallUserUpdateDTO;
import ltd.newbee.mall.controller.vo.MallUserVO;

public interface MallUserService {
    /**
     * 用户注册（统一契约：失败必抛异常，异常消息即失败原因）
     *
     * @param loginName
     * @param password
     */
    void register(String loginName, String password);

    /**
     * pro 版新增：REST 登录，校验通过后签发 Token
     * 失败必抛异常（复用内部 login() 的统一契约），成功只返回 token 字符串
     */
    String loginAndIssueToken(MallUserLoginDTO dto);

    /**
     * pro 版新增：REST 登出，删除 Token（幂等）
     */
    void logoutByToken(String token);

    /**
     * pro 版新增：按 userId 查用户信息（REST GET /info 用）
     * 统一契约：用户不存在抛异常
     */
    MallUserVO getUserById(Long userId);

    /**
     * 修改用户资料（昵称/签名/收货地址，仅更新传入的非空字段）并返回最新用户信息
     * 统一契约：用户不存在或更新失败必抛异常
     *
     * @param userId     当前登录用户 ID
     * @param updateDTO  待更新的资料字段
     * @return 更新后的用户信息（不含密码）
     */
    MallUserVO updateUserInfo(Long userId, MallUserUpdateDTO updateDTO);
}
