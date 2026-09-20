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
import ltd.newbee.mall.controller.dto.AdminNameUpdateDTO;
import ltd.newbee.mall.controller.dto.AdminPasswordUpdateDTO;
import ltd.newbee.mall.controller.dto.AdminUserLoginDTO;
import ltd.newbee.mall.entity.AdminUser;
import ltd.newbee.mall.controller.vo.AdminLoginVO;
import ltd.newbee.mall.entity.AdminToken;
import ltd.newbee.mall.mapper.AdminMapper;
import ltd.newbee.mall.mapper.AdminTokenMapper;
import ltd.newbee.mall.service.AdminService;
import ltd.newbee.mall.util.MD5Util;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    /** 为设置 Token 有效期：7 天 */
    private static final long TOKEN_EXPIRE_HOURS = 7 * 24L;

    private final AdminMapper adminMapper;
    private final AdminTokenMapper adminTokenMapper;

    @Override
    public AdminUser login(String userName, String password) {
        String passwordMd5 = MD5Util.MD5Encode(password, "UTF-8");
        return adminMapper.login(userName, passwordMd5);
    }

    @Override
    public AdminUser getUserDetailById(Integer adminUserId) {
        return adminMapper.selectByPrimaryKey(adminUserId);
    }

    @Override
    public AdminLoginVO getAdminUserById(Integer adminUserId) {
        AdminUser adminUser = adminMapper.selectByPrimaryKey(adminUserId);
        if (adminUser == null) {
            return null;
        }
        AdminLoginVO adminLoginVO = new AdminLoginVO();
        adminLoginVO.setAdminUserId(adminUser.getAdminUserId());
        adminLoginVO.setLoginUserName(adminUser.getLoginUserName());
        adminLoginVO.setNickName(adminUser.getNickName());
        return adminLoginVO;
    }

    @Override
    public Boolean updatePassword(Integer adminUserId, String originalPassword, String newPassword) {
        AdminUser admin = adminMapper.selectByPrimaryKey(adminUserId);
        //当前用户非空才可以进行更改
        if (admin != null) {
            String originalPasswordMd5 = MD5Util.MD5Encode(originalPassword, "UTF-8");
            String newPasswordMd5 = MD5Util.MD5Encode(newPassword, "UTF-8");
            //比较原密码是否正确
            if (originalPasswordMd5.equals(admin.getLoginPassword())) {
                //设置新密码并修改
                admin.setLoginPassword(newPasswordMd5);
                if (adminMapper.updateByPrimaryKeySelective(admin) > 0) {
                    //修改成功则返回true
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Boolean updateName(Integer adminUserId, String loginUserName, String nickName) {
        AdminUser admin = adminMapper.selectByPrimaryKey(adminUserId);
        //当前用户非空才可以进行更改
        if (admin != null) {
            //设置新名称并修改
            admin.setLoginUserName(loginUserName);
            admin.setNickName(nickName);
            if (adminMapper.updateByPrimaryKeySelective(admin) > 0) {
                //修改成功则返回true
                return true;
            }
        }
        return false;
    }

    @Override
    public String loginAndIssueToken(AdminUserLoginDTO dto) {
        // 复用原有校验逻辑（MD5 + locked=0 过滤），不改动存量行为
        AdminUser adminUser = login(dto.getUserName(), dto.getPassword());
        if (adminUser == null) {
            // 与原版页面登录的提示语保持一致
            throw new NewBeeMallException("登录失败，请检查用户名和密码");
        }

        // 签发 Token：UUID 去横线，入库并设置 7 天过期
        String token = UUID.randomUUID().toString().replace("-", "");
        AdminToken adminToken = new AdminToken();
        adminToken.setToken(token);
        adminToken.setAdminUserId(adminUser.getAdminUserId());
        adminToken.setExpiresAt(new Date(System.currentTimeMillis() + TOKEN_EXPIRE_HOURS * 3600 * 1000));
        adminToken.setCreateTime(new Date());
        adminTokenMapper.insert(adminToken);

        return token;
    }

    @Override
    public void updatePassword(Integer adminUserId, AdminPasswordUpdateDTO dto, String token) {
        // 跨字段校验（@Valid 只管单字段，管不了字段之间的关系）
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new NewBeeMallException("两次输入的密码不一致");
        }
        if (dto.getOriginalPassword().equals(dto.getNewPassword())) {
            throw new NewBeeMallException("新密码不能与原密码相同");
        }

        // 复用原有校验逻辑（MD5 比对）
        Boolean success = updatePassword(adminUserId, dto.getOriginalPassword(), dto.getNewPassword());
        if (!success) {
            throw new NewBeeMallException("原密码错误");
        }

        // 改密码后强制重新登录：删除当前 Token
        adminTokenMapper.deleteByToken(token);
    }

    @Override
    public AdminLoginVO updateName(Integer adminUserId, AdminNameUpdateDTO dto) {
        Boolean success = updateName(adminUserId, dto.getLoginUserName(), dto.getNickName());
        if (!success) {
            throw new NewBeeMallException("修改失败");
        }
        return getAdminUserById(adminUserId);
    }

    //TODO此处以后看情况可以考虑一下逻辑删除，
    // 不过数据库登录与登出在mysql数据库有“踢人下线”的逻辑，后期TT作者思考一下
    @Override
    public void logoutByToken(String token) {
        adminTokenMapper.deleteByToken(token);
    }
}
