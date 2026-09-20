package ltd.newbee.mall.controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ltd.newbee.mall.controller.dto.AdminPasswordUpdateDTO;
import ltd.newbee.mall.controller.dto.AdminProfileUpdateDTO;
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
     * - REST 版：从 request 取 adminUserId（拦截器放入），改成功后 Token 仍然有效
     *   （如果想让改密码后强制重登，可以在这里删 Token）
     *
     * @PutMapping：RESTful 约定，PUT = 修改资源
     */
    @PutMapping("/password")
    public Result updatePassword(HttpServletRequest request,
                                 @Valid @RequestBody AdminPasswordUpdateDTO dto) {
        Integer adminUserId = (Integer) request.getAttribute("adminUserId");
        if (adminUserId == null) {
            throw new NewBeeMallException("未登录");
        }

        // 两次密码一致性校验（@Valid 管不了跨字段逻辑）
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new NewBeeMallException("两次输入的密码不一致");
        }

        // 新密码不能和原密码相同
        if (dto.getOriginalPassword().equals(dto.getNewPassword())) {
            throw new NewBeeMallException("新密码不能与原密码相同");
        }

        // 复用原项目的 Service 方法
        Boolean success = adminService.updatePassword(
                adminUserId, dto.getOriginalPassword(), dto.getNewPassword());
        if (!success) {
            // 原项目返回 false 可能是"原密码错误"或"用户不存在"，统一提示
            throw new NewBeeMallException("原密码错误");
        }

        // 改密码后建议重新登录：删掉当前 Token，让前端重新走登录流程
        String token = extractToken(request);
        adminService.logoutByToken(token);

        return ResultGenerator.genSuccessResult();
    }

    // ==================== 修改昵称/登录名 ====================

    /**
     * 修改当前登录管理员的昵称和登录名
     *
     * 对比原项目 AdminController.nameUpdate()：
     * - 页面版：从 Session 取 adminUserId，返回 "success"/"修改失败" 字符串
     * - REST 版：从 request 取 adminUserId，失败抛异常（统一异常处理转 JSON）
     *
     * @PutMapping：修改资源用 PUT
     */
    @PutMapping("/profile")
    public Result<AdminLoginVO> updateProfile(HttpServletRequest request,
                                               @Valid @RequestBody AdminProfileUpdateDTO dto) {
        Integer adminUserId = (Integer) request.getAttribute("adminUserId");
        if (adminUserId == null) {
            throw new NewBeeMallException("未登录");
        }

        // 复用原项目的 Service 方法
        Boolean success = adminService.updateName(
                adminUserId, dto.getLoginUserName(), dto.getNickName());
        if (!success) {
            throw new NewBeeMallException("修改失败");
        }

        // 返回更新后的管理员信息，前端可以直接刷新显示
        return ResultGenerator.genSuccessResult(adminService.getAdminUserById(adminUserId));
    }
}
