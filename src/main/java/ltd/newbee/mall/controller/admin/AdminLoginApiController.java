package ltd.newbee.mall.controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ltd.newbee.mall.controller.dto.AdminUserLoginDTO;
import ltd.newbee.mall.controller.vo.AdminUserProfileVO;
import ltd.newbee.mall.service.AdminService;
import ltd.newbee.mall.util.Result;
import ltd.newbee.mall.util.ResultGenerator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ltd.newbee.mall.common.NewBeeMallException;

/**
 * 管理端 REST 登录接口（pro 版新增）
 *
 * 对比原项目 AdminController（页面版）：
 * 1. 页面版：登录成功 return "redirect:/admin/index"（服务端跳转），Session 存登录态
 *    REST 版：登录成功只返回 token，前端自己保存 token、自己跳页面
 * 2. 页面版：Session 认证；REST 版：Authorization: Bearer <token>，由 AdminTokenInterceptor 校验
 * 3. REST 版暂不做验证码（Session 版验证码存在 Session 里，纯 API 下不适用，后续单独做）
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminLoginApiController {

    private final AdminService adminService;

    /** 登录：校验通过只返回 token，需要用户资料调 /info */
    @PostMapping("/login")
    public Result<String> login(@Valid @RequestBody AdminUserLoginDTO dto) {
        String token = adminService.loginAndIssueToken(dto);
        Result<String> result = new Result<>(200, "SUCCESS");
        result.setData(token);
        return result;
    }

    /** 登出：删除 Token（幂等，重复登出也返回成功） */
    @PostMapping("/logout")
    public Result logout(HttpServletRequest request) {
        String token = extractToken(request);
        adminService.logoutByToken(token);
        return ResultGenerator.genSuccessResult();
    }

    /** 当前登录管理员信息：Token 拦截器校验通过后，adminUserId 已放入 request */
    @GetMapping("/info")
    public Result<AdminUserProfileVO> info(HttpServletRequest request) {
        Integer adminUserId = (Integer) request.getAttribute("adminUserId");
        if (adminUserId == null) {
            throw new NewBeeMallException("未登录");
        }
        return ResultGenerator.genSuccessResult(adminService.getAdminUserProfileById(adminUserId));
    }

    /**
     * 从 Authorization 头提取 Token
     * 约定格式：Authorization: Bearer <token>
     */
    private String extractToken(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new NewBeeMallException("未登录");
        }
        return auth.substring(7);
    }
}
