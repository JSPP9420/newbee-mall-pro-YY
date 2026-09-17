package ltd.newbee.mall.interceptor;

import ltd.newbee.mall.mapper.AdminPermissionMapper;
import ltd.newbee.mall.entity.AdminPermission;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Locale;

public class AdminPermissionInterceptor implements HandlerInterceptor {
    private final AdminPermissionMapper adminPermissionMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public AdminPermissionInterceptor(AdminPermissionMapper adminPermissionMapper) {
        this.adminPermissionMapper = adminPermissionMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        Object adminUserId = request.getSession().getAttribute("adminUserId");
        if (!(adminUserId instanceof Integer)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        String method = request.getMethod().toUpperCase(Locale.ROOT);
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = requestUri.substring(contextPath.length());
        List<AdminPermission> permissions = adminPermissionMapper
                .findByAdminUserId((Integer) adminUserId);
        for (AdminPermission permission : permissions) {
            if (method.equalsIgnoreCase(permission.getHttpMethod())
                    && StringUtils.hasText(permission.getRequestPath())
                    && pathMatcher.match(permission.getRequestPath(), path)) {
                return true;
            }
        }
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        return false;
    }
}
