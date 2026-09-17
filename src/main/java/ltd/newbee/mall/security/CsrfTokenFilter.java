package ltd.newbee.mall.security;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class CsrfTokenFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // pro 版：REST API（/api/**）使用 Authorization 头做 Token 认证，
        // 不依赖 Cookie（CSRF 攻击正是利用浏览器自动携带 Cookie），
        // 因此天然免疫 CSRF，无需 CSRF 校验，直接放行
        if (request.getServletPath().startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        HttpSession session = request.getSession(true);
        String expectedToken = CsrfTokenService.getOrCreateToken(session);
        request.setAttribute(CsrfTokenService.REQUEST_ATTRIBUTE, expectedToken);

        if (isSafeMethod(request) || isValidToken(request, expectedToken)) {
            filterChain.doFilter(request, response);
            return;
        }

        response.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF token 校验失败");
    }

    private boolean isSafeMethod(HttpServletRequest request) {
        String method = request.getMethod();
        return "GET".equalsIgnoreCase(method)
                || "HEAD".equalsIgnoreCase(method)
                || "OPTIONS".equalsIgnoreCase(method);
    }

    private boolean isValidToken(HttpServletRequest request, String expectedToken) {
        String submittedToken = request.getHeader(CsrfTokenService.REQUEST_HEADER);
        if (submittedToken == null) {
            submittedToken = request.getParameter(CsrfTokenService.REQUEST_PARAMETER);
        }
        return submittedToken != null
                && MessageDigest.isEqual(
                expectedToken.getBytes(StandardCharsets.UTF_8),
                submittedToken.getBytes(StandardCharsets.UTF_8));
    }
}
