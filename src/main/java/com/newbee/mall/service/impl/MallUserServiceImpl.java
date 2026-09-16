package com.newbee.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.newbee.mall.common.ResultCode;
import com.newbee.mall.controller.dto.UserLoginDTO;
import com.newbee.mall.controller.dto.UserRegisterDTO;
import com.newbee.mall.controller.vo.LoginVO;
import com.newbee.mall.controller.vo.UserVO;
import com.newbee.mall.entity.MallToken;
import com.newbee.mall.entity.MallUser;
import com.newbee.mall.exception.BusinessException;
import com.newbee.mall.mapper.MallTokenMapper;
import com.newbee.mall.mapper.MallUserMapper;
import com.newbee.mall.service.MallUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 用户服务实现 — 业务逻辑的唯一归属地
 *
 * 对比原项目改进：
 * 1. register 查重 + 唯一索引双保险（并发安全）
 * 2. 登录失败不区分"用户不存在"和"密码错误"（不暴露账号是否存在）
 * 3. 遇到问题直接抛 BusinessException，不返回字符串
 */
@Service
public class MallUserServiceImpl implements MallUserService {

    private final MallUserMapper userMapper;
    private final MallTokenMapper tokenMapper;
    private final PasswordEncoder passwordEncoder;

    // 构造器注入（不用 @Autowired，Spring 4.3+ 自动注入）
    public MallUserServiceImpl(MallUserMapper userMapper,
                               MallTokenMapper tokenMapper,
                               PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.tokenMapper = tokenMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void register(UserRegisterDTO dto) {
        // 1. 查重（给用户友好提示）
        MallUser existUser = userMapper.selectOne(
                new QueryWrapper<MallUser>().eq("login_name", dto.getLoginName())
        );
        if (existUser != null) {
            throw new BusinessException(ResultCode.USER_EXISTS);
        }

        // 2. 构建用户
        MallUser user = new MallUser();
        user.setLoginName(dto.getLoginName());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickName(dto.getLoginName()); // 默认昵称 = 登录名

        // 3. 插入（唯一索引兜底，万一并发时两人同时注册同名）
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ResultCode.USER_EXISTS);
        }
        // register 成功就正常返回，不返回字符串 "success"
    }

    @Override
    public LoginVO login(UserLoginDTO dto) {
        // 1. 查用户
        MallUser user = userMapper.selectOne(
                new QueryWrapper<MallUser>().eq("login_name", dto.getLoginName())
        );

        // 2. 用户不存在 → 返回"用户名或密码错误"（不暴露账号是否存在）
        if (user == null) {
            throw new BusinessException(ResultCode.LOGIN_FAILED);
        }

        // 3. 账号锁定 → 单独提示（让用户知道为啥登不了）
        if (user.getStatus() == 1) {
            throw new BusinessException(ResultCode.USER_LOCKED);
        }

        // 4. 校验密码（BCrypt：每次加密结果不同，用 matches 方法比较）
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.LOGIN_FAILED);
        }

        // 5. 生成 Token（UUID），存数据库
        String token = UUID.randomUUID().toString().replace("-", "");
        MallToken tokenEntity = new MallToken();
        tokenEntity.setToken(token);
        tokenEntity.setUserId(user.getId());
        tokenEntity.setExpiresAt(LocalDateTime.now().plusDays(7));
        tokenMapper.insert(tokenEntity);

        // 6. 返回 LoginVO（token + 用户信息，不含密码）
        return LoginVO.of(token, toUserVO(user));
    }

    @Override
    public void logout(String token) {
        // 删除 token 记录，删不删到都返回成功（幂等）
        tokenMapper.delete(
                new QueryWrapper<MallToken>().eq("token", token)
        );
    }

    @Override
    public UserVO getCurrentUserByToken(String token) {
        // 1. 查 token
        MallToken tokenEntity = tokenMapper.selectOne(
                new QueryWrapper<MallToken>().eq("token", token)
        );
        if (tokenEntity == null) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        // 2. 检查是否过期
        if (tokenEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        // 3. 查用户
        MallUser user = userMapper.selectById(tokenEntity.getUserId());
        if (user == null) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        return toUserVO(user);
    }

    /** Entity → VO 转换（隔离数据库字段和前端可见字段） */
    private UserVO toUserVO(MallUser user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        vo.setUserId(user.getId());
        return vo;
    }
}
