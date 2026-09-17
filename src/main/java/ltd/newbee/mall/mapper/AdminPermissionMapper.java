package ltd.newbee.mall.mapper;

import ltd.newbee.mall.entity.AdminPermission;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AdminPermissionMapper {
    List<AdminPermission> findByAdminUserId(@Param("adminUserId") Integer adminUserId);
}
