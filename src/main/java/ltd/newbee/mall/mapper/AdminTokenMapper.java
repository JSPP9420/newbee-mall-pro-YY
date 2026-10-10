package ltd.newbee.mall.mapper;

import ltd.newbee.mall.entity.AdminToken;
import org.apache.ibatis.annotations.Param;

/**
 * 管理员 Token Mapper（pro 版新增）
 * 与原项目风格一致：接口 + XML（resources/mapper/AdminTokenMapper.xml）
 */
public interface AdminTokenMapper {

    int insert(AdminToken adminToken);

    AdminToken selectByToken(@Param("token") String token);

    /**
     * 按 Token 删除（登出/单个会话失效）
     */
    int deleteByToken(@Param("token") String token);

    /**
     * 按管理员 ID 删除其全部 Token（安全兜底版#2）
     * 凭据变更（改密码/改登录名）时吊销所有会话，不只是当前这台设备——
     * 只删当前 Token 的话，其他设备登录的 Token 依然有效，等于"改了密码没被踢下线"
     */
    int deleteByAdminUserId(@Param("adminUserId") Integer adminUserId);
}
