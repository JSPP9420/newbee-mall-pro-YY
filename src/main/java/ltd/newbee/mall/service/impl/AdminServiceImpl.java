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
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

    /**
     * 登录：纯 BCrypt 校验（MD5 兼容与懒升级已随存量数据迁移完成而移除）
     *
     * 为什么不能在 SQL 里比对密码？
     * 原项目 login() 是 WHERE login_password = #{md5}，等值比对。
     * 但 BCrypt 每次加密结果都不同（含随机盐），没法写等值 SQL，
     * 所以改为：先按用户名查出记录，再在 Java 里用 matches() 验证。
     */
    @Override
    public AdminUser login(String userName, String password) {
        AdminUser adminUser = adminMapper.selectByLoginUserName(userName);
        if (adminUser == null) {
            return null;
        }
        return passwordEncoder.matches(password, adminUser.getLoginPassword()) ? adminUser : null;
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
    public void updatePassword(Integer adminUserId, String originalPassword, String newPassword) {
        AdminUser adminUser = adminMapper.selectByPrimaryKey(adminUserId);
        if (adminUser == null) {
            throw new NewBeeMallException("用户不存在");
        }
        //验证原密码（纯 BCrypt）
        if (!passwordEncoder.matches(originalPassword, adminUser.getLoginPassword())) {
            throw new NewBeeMallException("原密码错误");
        }
        //新密码用 BCrypt 存储
        adminUser.setLoginPassword(passwordEncoder.encode(newPassword));
        adminMapper.updateByPrimaryKeySelective(adminUser);
    }

    @Override
    public void updateName(Integer adminUserId, String loginUserName, String nickName) {
        AdminUser adminUser = adminMapper.selectByPrimaryKey(adminUserId);
        if (adminUser == null) {
            throw new NewBeeMallException("用户不存在");
        }
        adminUser.setLoginUserName(loginUserName);
        adminUser.setNickName(nickName);
        adminMapper.updateByPrimaryKeySelective(adminUser);
    }

    @Override
    public String loginAndIssueToken(AdminUserLoginDTO dto) {
        // 复用 login()：纯 BCrypt 校验 + locked=0 过滤
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

    /**
     *一个字段变更后要不要删 Token，看它是否参与身份验证 :
     * 参与验证的凭据（密码，严格说还有登录用户名）→ 变更 = 信任链断裂 → 必须删 Token，强制重新证明身份；
     * 纯展示资料（昵称、头像等）→ 不影响验证链 → 不删，会话照常。
     */
    @Override
    public void updatePassword(Integer adminUserId, AdminPasswordUpdateDTO dto, String token) {
        // 跨字段校验（@Valid 只管单字段，管不了字段之间的关系）
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new NewBeeMallException("两次输入的密码不一致");
        }
        if (dto.getOriginalPassword().equals(dto.getNewPassword())) {
            throw new NewBeeMallException("新密码不能与原密码相同");
        }

        // 核心校验与更新（失败直接抛异常：用户不存在/原密码错误）
        updatePassword(adminUserId, dto.getOriginalPassword(), dto.getNewPassword());

        // 改密码后强制重新登录：删除当前 Token
        adminTokenMapper.deleteByToken(token);
    }

    @Override
    public AdminLoginVO updateName(Integer adminUserId, AdminNameUpdateDTO dto) {
        updateName(adminUserId, dto.getLoginUserName(), dto.getNickName());
        return getAdminUserById(adminUserId);
    }

    //TODO此处以后看情况可以考虑一下逻辑删除，
    // 不过数据库登录与登出在mysql数据库有“踢人下线”的逻辑，后期TT作者思考一下
    @Override
    public void logoutByToken(String token) {
        adminTokenMapper.deleteByToken(token);
    }
}
