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

    /**
     * 登录（统一契约：失败必抛异常，异常消息即失败原因；成功返回管理员实体）
     * 用户名不存在、密码错误、账号锁定统一提示，防账号枚举
     *
     * @param userName 登录用户名
     * @param password 明文密码
     * @return 登录成功的管理员实体
     */
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

    AdminLoginVO getAdminUserById(Integer adminUserId);

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
