package ltd.newbee.mall.mapper;

import ltd.newbee.mall.entity.MallUserToken;
import org.apache.ibatis.annotations.Param;

/**
 * 用户端 Token Mapper（pro 版新增）
 * 与 AdminTokenMapper 同套路：接口 + XML（resources/mapper/MallUserTokenMapper.xml）
 */
public interface MallUserTokenMapper {

    int insert(MallUserToken mallUserToken);

    MallUserToken selectByToken(@Param("token") String token);

    int deleteByToken(@Param("token") String token);
}
