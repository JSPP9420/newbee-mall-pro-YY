package ltd.newbee.mall.mapper;

import ltd.newbee.mall.entity.AdminToken;
import org.apache.ibatis.annotations.Param;

/**
 * 管理员 Token Mapper（pro 版新增）
 * 与原项目风格一致：接口 + XML（resources/mapper/AdminTokenMapper.xml）
 */
public interface AdminTokenMapper {

    int insert(AdminToken record);

    AdminToken selectByToken(@Param("token") String token);

    int deleteByToken(@Param("token") String token);
}
