package ltd.newbee.mall.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import ltd.newbee.mall.entity.MallUserToken;
import ltd.newbee.mall.mapper.MallUserTokenMapper;
import ltd.newbee.mall.util.Result;
import ltd.newbee.mall.util.ResultGenerator;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * 用户端 REST API 的 Token 认证拦截器（pro 版新增）
 *
 * 与管理端 AdminTokenInterceptor 同套路：
 * - 从 Authorization: Bearer <token> 头解析 Token，去数据库校验（存在且未过期）
 * - 校验通过把 userId 放进 request attribute，后续 Controller 直接取用
 * - 校验失败返回 401 JSON（与原项目 Result 格式一致）
 *
 * 对比页面版 NewBeeMallLoginInterceptor：Session 找 loginUser，没有重定向登录页；
 * REST 版没有页面可跳，只能返回 401 让前端自己处理
 */
@Component
@RequiredArgsConstructor
public class MallUserTokenInterceptor implements HandlerInterceptor {

    private final MallUserTokenMapper mallUserTokenMapper;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String auth = request.getHeader("Authorization");
        String token = null;
        //Authorization: Bearer <token> 是 HTTP 官方标准认证头格式
        if (auth != null && auth.startsWith("Bearer ")) {
            token = auth.substring(7);
        }
        if (token == null || token.isEmpty()) {
            return reject(response);
        }

        MallUserToken userToken = mallUserTokenMapper.selectByToken(token);
        if (userToken == null || userToken.getExpiresAt().before(new Date())) {
            return reject(response);
        }

        // 认证通过：把用户 id 传给后续 Controller
        request.setAttribute("userId", userToken.getUserId());
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
