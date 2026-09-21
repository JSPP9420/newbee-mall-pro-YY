/**
 * 严肃声明：
 * 开源版本请务必保留此注释头信息，若删除我方将保留所有法律责任追究！
 * 本系统已申请软件著作权，受国家版权局知识产权以及国家计算机软件著作权保护！
 * 可正常分享和学习源码，不得用于违法犯罪活动，违者必究！
 * Copyright (c) 2019-2020 十三 all rights reserved.
 * 版权所有，侵权必究！
 */
package ltd.newbee.mall.mapper;

import ltd.newbee.mall.entity.AdminUser;
import org.apache.ibatis.annotations.Param;

public interface AdminMapper {
    int insert(AdminUser record);

    int insertSelective(AdminUser record);

    /**
     * 登陆方法
     *
     * @param userName
     * @param password
     * @return
     */
    AdminUser login(@Param("userName") String userName, @Param("password") String password);

    AdminUser selectByPrimaryKey(Integer adminUserId);

    /**
     * pro 版新增：按登录名查管理员（含锁定状态过滤）
     *
     * 为什么需要它？原 login() 把密码比对放在 SQL 里（login_password = #{password}），
     * 但 BCrypt 每次加密结果不同、无法在 SQL 里等值比对，
     * 所以必须先按用户名查出整条记录，再在 Java 里用 PasswordEncoder.matches() 验证
     */
    AdminUser selectByLoginUserName(@Param("userName") String userName);

    int updateByPrimaryKeySelective(AdminUser record);

    int updateByPrimaryKey(AdminUser record);
}