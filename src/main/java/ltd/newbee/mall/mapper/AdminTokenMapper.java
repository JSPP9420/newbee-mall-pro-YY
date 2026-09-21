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
    //TODO此方法不够严谨，有待优化，我认为应该需要删除所有个人信息  或者  账号与密码。
    int deleteByToken(@Param("token") String token);
}
