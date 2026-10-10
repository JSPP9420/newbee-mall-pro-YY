package ltd.newbee.mall.controller.mall;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ltd.newbee.mall.common.NewBeeMallException;
import ltd.newbee.mall.controller.dto.MallUserLoginDTO;
import ltd.newbee.mall.controller.dto.MallUserRegisterDTO;
import ltd.newbee.mall.controller.dto.MallUserUpdateDTO;
import ltd.newbee.mall.controller.vo.MallUserVO;
import ltd.newbee.mall.service.MallUserService;
import ltd.newbee.mall.util.Result;
import ltd.newbee.mall.util.ResultGenerator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端 REST 接口（pro 版新增）
 *
 * 对比原项目 MallUserController（页面版）：
 * 1. 页面版：Session 存登录态 + 验证码 + CSRF Token；REST 版：Authorization: Bearer <token>，
 *    由 MallUserTokenInterceptor 校验（Token 认证天然免疫 CSRF，CsrfTokenFilter 已放行 /api/**）
 * 2. 页面版：登录成功后服务端 return 页面；REST 版：只返回 token，前端自己保存、自己跳转
 * 3. 页面版登录/注册需要图形验证码（验证码存在 Session 里）；REST 版暂不做验证码，
 *    纯 Token API 下 Session 验证码不适用（与管理端 AdminLoginApiController 同决策）
 * 4. 错误契约：失败由 Service 抛 NewBeeMallException，经全局异常处理器统一返回 JSON
 *
 * 页面版中的 /personal、/personal/addresses 等纯页面路由不属于 REST 范畴，不迁移
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mall")
public class MallUserApiController {

    private final MallUserService mallUserService;

    /**
     * 注册：成功即代表可登录，前端引导去登录页
     * 失败（登录名已存在等）由 Service 抛异常，经全局异常处理器返回
     * 入参用 MallUserRegisterDTO（带强度校验），与登录 DTO 分离（安全加固 #6）
     */
    @PostMapping("/register")
    public Result register(@Valid @RequestBody MallUserRegisterDTO dto) {
        mallUserService.register(dto.getLoginName(), dto.getPassword());
        return ResultGenerator.genSuccessResult();
    }

    /** 登录：校验通过只返回 token，需要用户资料调 GET /info */
    @PostMapping("/login")
    public Result<String> login(@Valid @RequestBody MallUserLoginDTO dto) {
        String token = mallUserService.loginAndIssueToken(dto);
        Result<String> result = new Result<>(200, "SUCCESS");
        result.setData(token);
        return result;
    }

    /** 登出：删除 Token（幂等，重复登出也返回成功） */
    @PostMapping("/logout")
    public Result logout(HttpServletRequest request) {
        mallUserService.logoutByToken(extractToken(request));
        return ResultGenerator.genSuccessResult();
    }

    /** 当前登录用户信息：Token 拦截器校验通过后，userId 已放入 request */
    @GetMapping("/info")
    public Result<MallUserVO> info(HttpServletRequest request) {
        return ResultGenerator.genSuccessResult(mallUserService.getUserById(requireUserId(request)));
    }

    /**
     * 修改当前登录用户资料（昵称/签名/收货地址）
     *
     * 对比页面版 /personal/updateInfo：从 Session 取用户改为从 request 取
     * （拦截器放入的 userId），资料修改不影响身份验证链，无需删除 Token
     */
    @PutMapping("/info")
    public Result<MallUserVO> updateInfo(HttpServletRequest request,
                                         @Valid @RequestBody MallUserUpdateDTO dto) {
        return ResultGenerator.genSuccessResult(
                mallUserService.updateUserInfo(requireUserId(request), dto));
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

    /**
     * 取当前登录用户 ID（Token 拦截器校验通过后放入 request）
     * 拦截器与 Controller 解耦，不假设拦截器一定执行过，防御性判空
     */
    private Long requireUserId(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new NewBeeMallException("未登录");
        }
        return userId;
    }
}
