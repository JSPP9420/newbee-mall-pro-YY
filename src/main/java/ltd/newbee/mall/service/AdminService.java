/**
 * 严肃声明：
 * 开源版本请务必保留此注释头信息，若删除我方将保留所有法律责任追究！
 * 本系统已申请软件著作权，受国家版权局知识产权以及国家计算机软件著作权保护！
 * 可正常分享和学习源码，不得用于违法犯罪活动，违者必究！
 * Copyright (c) 2019-2020 十三 all rights reserved.
 * 版权所有，侵权必究！
 */
package ltd.newbee.mall.service;

import ltd.newbee.mall.controller.dto.AdminNameUpdateDTO;
import ltd.newbee.mall.controller.dto.AdminPasswordUpdateDTO;
import ltd.newbee.mall.controller.dto.AdminUserLoginDTO;
import ltd.newbee.mall.controller.vo.AdminLoginVO;
import ltd.newbee.mall.entity.AdminUser;

public interface AdminService {

    AdminUser login(String userName, String password);

    /**
     * pro 版新增：REST 登录，校验通过后签发 Token
     * 只返回 token 字符串，前端需要用户资料时调 /info 接口
     */
    String loginAndIssueToken(AdminUserLoginDTO dto);

    /**
     * pro 版新增：REST 登出，删除 Token
     */
    void logoutByToken(String token);

    /**
     * 获取用户信息
     *
     * @param adminUserId
     * @return
     */
    AdminUser getUserDetailById(Integer adminUserId);

    AdminLoginVO getAdminUserById(Integer adminUserId);

    /**
     * 修改当前登录用户的密码
     *
     * @param adminUserId
     * @param originalPassword
     * @param newPassword
     * @return
     */
    Boolean updatePassword(Integer adminUserId, String originalPassword, String newPassword);

    /**
     * 修改当前登录用户的名称信息
     *
     * @param adminUserId
     * @param loginUserName
     * @param nickName
     * @return
     */
    Boolean updateName(Integer adminUserId, String loginUserName, String nickName);

    /**
     * pro 版新增：REST 修改密码（业务校验在 Service 层）
     * 校验规则：两次密码一致、新旧密码不同、原密码正确
     * 修改成功后删除当前 Token，强制重新登录
     *
     * @param adminUserId 当前登录管理员
     * @param dto         修改密码入参
     * @param token       当前 Token（改密成功后删除）
     */
    void updatePassword(Integer adminUserId, AdminPasswordUpdateDTO dto, String token);

    /**
     * pro 版新增：REST 修改登录名/昵称，返回更新后的管理员信息
     */
    AdminLoginVO updateName(Integer adminUserId, AdminNameUpdateDTO dto);

}
