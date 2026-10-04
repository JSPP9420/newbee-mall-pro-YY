package ltd.newbee.mall.controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ltd.newbee.mall.controller.dto.AdminPasswordUpdateDTO;
import ltd.newbee.mall.controller.dto.AdminNameUpdateDTO;
import ltd.newbee.mall.controller.dto.AdminUserLoginDTO;
import ltd.newbee.mall.controller.vo.AdminLoginVO;
import ltd.newbee.mall.service.AdminService;
import ltd.newbee.mall.util.Result;
import ltd.newbee.mall.util.ResultGenerator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
//TODO少了老板需要管理的会员管理，比如数据分析
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminLoginApiController {

    private final AdminService adminService;

    /** 登录：校验通过只返回 token，需要管理员信息调 /info */
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
    public Result<AdminLoginVO> info(HttpServletRequest request) {
        Integer adminUserId = (Integer) request.getAttribute("adminUserId"); // 捡起拦截器留的记号
        /**
         * 为什么拦截器已经验证过Token，这里还要判断 adminUserId == null？
         *
         * 1. 防御性编程：拦截器和Controller是解耦的，Controller不能假设拦截器一定执行了
         * 2. 可能的异常情况：
         *    - 拦截器配置变更，此接口被移出拦截范围
         *    - Token合法但request中没有adminUserId字段（解析出来是null）
         *    - 单元测试/Postman直接调用，绕过了拦截器
         * 3. Fail-Fast原则：尽早发现问题，返回明确的"未登录"提示，而不是抛出NullPointerException
         *
         * 一句话：各管各的，谁也别完全相信谁。
         */
        if (adminUserId == null) {
            throw new NewBeeMallException("未登录");
        }
        return ResultGenerator.genSuccessResult(adminService.getAdminUserById(adminUserId));
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

    // ==================== 修改密码 ====================

    /**
     * 修改当前登录管理员的密码
     *
     * 对比原项目 AdminController.passwordUpdate()：
     * - 页面版：从 Session 取 adminUserId，改成功后清 Session 强制重新登录
     * - REST 版：从 request 取 adminUserId（拦截器放入），业务校验在 Service，
     *   改成功后由 Service 删除当前 Token 强制重新登录
     *
     * @PutMapping：RESTful 约定，PUT = 修改资源
     */
    @PutMapping("/password")
    public Result updatePassword(HttpServletRequest request,
                                 @Valid @RequestBody AdminPasswordUpdateDTO dto) {
        adminService.updatePassword(requireAdminUserId(request), dto, extractToken(request));
        return ResultGenerator.genSuccessResult();
    }

    // ==================== 修改登录名/昵称 ====================

    /**
     * 修改当前登录管理员的登录名和昵称，返回更新后的信息
     *
     * 对比原项目 AdminController.nameUpdate()：
     * - 页面版：从 Session 取 adminUserId，返回 "success"/"修改失败" 字符串
     * - REST 版：从 request 取 adminUserId，失败由 Service 抛异常（统一异常处理转 JSON）
     */
    @PutMapping("/name")
    public Result<AdminLoginVO> updateName(HttpServletRequest request,
                                           @Valid @RequestBody AdminNameUpdateDTO dto) {
        return ResultGenerator.genSuccessResult(adminService.updateName(requireAdminUserId(request), dto));
    }

    /**
     * 取当前登录管理员 ID（Token 拦截器校验通过后放入 request）
     * 拦截器与 Controller 解耦，不假设拦截器一定执行过，防御性判空
     */
    private Integer requireAdminUserId(HttpServletRequest request) {
        Integer adminUserId = (Integer) request.getAttribute("adminUserId");
        if (adminUserId == null) {
            throw new NewBeeMallException("未登录");
        }
        return adminUserId;
    }
}
