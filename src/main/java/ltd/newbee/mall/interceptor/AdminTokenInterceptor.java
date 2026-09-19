package ltd.newbee.mall.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import ltd.newbee.mall.entity.AdminToken;
import ltd.newbee.mall.mapper.AdminTokenMapper;
import ltd.newbee.mall.util.Result;
import ltd.newbee.mall.util.ResultGenerator;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * 管理端 REST API 的 Token 认证拦截器（pro 版新增）
 *
 * 对比 Session 版 AdminLoginInterceptor：
 * - Session 版：从 HttpSession 里找 loginUser，没有就重定向到登录页
 * - Token 版：从 Authorization 头解析 Bearer token，去数据库校验，没有就返回 401 JSON
 * <p>
 * 校验通过后把 adminUserId 放进 request attribute，后续 Controller 直接取用。
 * （后续可升级为 ThreadLocal 上下文，不必每次 request.getAttribute）
 */
@Component
@RequiredArgsConstructor
public class AdminTokenInterceptor implements HandlerInterceptor {

    private final AdminTokenMapper adminTokenMapper;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String auth = request.getHeader("Authorization");
        String token = null;
        if (auth != null && auth.startsWith("Bearer ")) {
            token = auth.substring(7);
        }
        if (token == null || token.isEmpty()) {
            return reject(response);
        }

        AdminToken adminToken = adminTokenMapper.selectByToken(token);
        if (adminToken == null || adminToken.getExpiresAt().before(new Date())) {
            return reject(response);
        }

        // 认证通过：把管理员 id 传给后续 Controller
        request.setAttribute("adminUserId", adminToken.getAdminUserId());
        return true;
    }

    /** 返回 401 统一 JSON，与原项目 Result 格式一致 */
    private boolean reject(HttpServletResponse response) throws Exception {
        Result result = ResultGenerator.genErrorResult(401, "未登录或Token已失效");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(result));
        return false;
    }
}
