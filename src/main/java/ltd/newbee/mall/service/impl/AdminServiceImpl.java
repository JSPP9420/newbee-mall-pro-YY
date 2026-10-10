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
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * 登录（统一契约：失败必抛异常）
     *
     * 为什么不能在 SQL 里比对密码？
     * 原项目 login() 是 WHERE login_password = #{md5}，等值比对。
     * 但 BCrypt 每次加密结果都不同（含随机盐），没法写等值 SQL，
     * 所以改为：先按用户名查出记录，再在 Java 里用 matches() 验证。
     *
     * 注意失败消息的顺序设计（与用户端 MallUserServiceImpl.login 同套路）：
     * - 用户不存在与密码错误统一提示，防账号枚举
     * - 锁定账号由 SQL 过滤 locked=0 实现，查不出来即同提示，不单独暴露锁定状态
     */
    @Override
    public AdminUser login(String userName, String password) {
        AdminUser adminUser = adminMapper.selectByLoginUserName(userName);
        if (adminUser == null || !passwordEncoder.matches(password, adminUser.getLoginPassword())) {
            throw new NewBeeMallException("登录失败，请检查用户名和密码");
        }
        return adminUser;
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

    /**
     * 核心改密逻辑（供 REST 版 updatePassword 复用，统一契约：失败必抛异常）
     */
    private void updatePassword(Integer adminUserId, String originalPassword, String newPassword) {
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

    /**
     * 核心改名逻辑（供 REST 版 updateName 复用，统一契约：失败必抛异常）
     *
     * Token 吊销的边界条件（安全加固 #5）：
     * - 只改昵称 → 不吊销 Token（纯展示资料，不参与身份验证链）
     * - 改了登录名 → 吊销该管理员全部 Token（登录名参与身份验证链，变更 = 信任链断裂）
     * 判断依据：更新前比较新旧登录名，避免"只改昵称也被踢下线"的误伤
     */
    private void updateName(Integer adminUserId, String loginUserName, String nickName) {
        AdminUser adminUser = adminMapper.selectByPrimaryKey(adminUserId);
        if (adminUser == null) {
            throw new NewBeeMallException("用户不存在");
        }
        boolean loginNameChanged = !adminUser.getLoginUserName().equals(loginUserName);
        if (loginNameChanged) {
            // 重名预检查（给友好提示用）；锁定账号占用的登录名此处查不到
            // （selectByLoginUserName 带 locked=0 过滤），漏网的由唯一索引兜底
            if (adminMapper.selectByLoginUserName(loginUserName) != null) {
                throw new NewBeeMallException("登录名已存在");
            }
        }
        adminUser.setLoginUserName(loginUserName);
        adminUser.setNickName(nickName);
        try {
            adminMapper.updateByPrimaryKeySelective(adminUser);
        } catch (DuplicateKeyException e) {
            // 兜底：撞 uk_login_user_name（预检查漏掉的锁定账号同名等场景），与购物车 V6 同套路
            throw new NewBeeMallException("登录名已存在");
        }
        if (loginNameChanged) {
            // 登录名是身份凭据，变更后吊销全部 Token，所有设备强制重新登录
            adminTokenMapper.deleteByAdminUserId(adminUserId);
        }
    }

    @Override
    public String loginAndIssueToken(AdminUserLoginDTO dto) {
        // 复用 login() 的统一契约：失败必抛异常（用户名或密码错误/账号锁定，防枚举统一提示）
        AdminUser adminUser = login(dto.getUserName(), dto.getPassword());

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
     *
     * 事务说明：改密码与吊销 Token 是两次写入（update + delete），无事务时各自独立提交，
     * 吊销那步失败会出现"密码已改但旧会话全部有效"的中间态，安全语义最怕的窗口——
     * 所以整个方法包进一个事务，要么都成功，要么都回滚
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(Integer adminUserId, AdminPasswordUpdateDTO dto) {
        // 跨字段校验（@Valid 只管单字段，管不了字段之间的关系）
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new NewBeeMallException("两次输入的密码不一致");
        }
        if (dto.getOriginalPassword().equals(dto.getNewPassword())) {
            throw new NewBeeMallException("新密码不能与原密码相同");
        }

        // 核心校验与更新（失败直接抛异常：用户不存在/原密码错误）
        updatePassword(adminUserId, dto.getOriginalPassword(), dto.getNewPassword());

        // 改密码后强制重新登录：吊销该管理员全部 Token（不只当前设备）
        adminTokenMapper.deleteByAdminUserId(adminUserId);
    }

    /**
     * 事务说明：改名（update）与登录名变化后的吊销 Token（delete）在同一事务，
     * 防"登录名已改但旧会话全部仍有效"的中间态，与 updatePassword 同理
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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
