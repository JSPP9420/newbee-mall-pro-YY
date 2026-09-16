package com.newbee.mall.controller;

import com.newbee.mall.common.Result;
import com.newbee.mall.controller.dto.UserLoginDTO;
import com.newbee.mall.controller.dto.UserRegisterDTO;
import com.newbee.mall.controller.vo.LoginVO;
import com.newbee.mall.controller.vo.UserVO;
import com.newbee.mall.exception.BusinessException;
import com.newbee.mall.common.ResultCode;
import com.newbee.mall.service.MallUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 用户接口
 *
 * M1a 阶段：Token 校验在 Controller 手动做（先理解流程）
 * M1c 阶段：抽到拦截器，Controller 会变干净
 */
@Tag(name = "用户", description = "注册、登录、登出、查看个人信息")
@RestController
@RequestMapping("/api/v1/user")
public class MallUserController {

    private final MallUserService userService;

    public MallUserController(MallUserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "注册")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody UserRegisterDTO dto) {
        userService.register(dto);
        return Result.success();
    }

    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody UserLoginDTO dto) {
        return Result.success(userService.login(dto));
    }

    @Operation(summary = "登出")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String token = extractToken(request);
        userService.logout(token);
        return Result.success();
    }

    @Operation(summary = "查看当前登录用户信息")
    @GetMapping("/me")
    public Result<UserVO> me(HttpServletRequest request) {
        String token = extractToken(request);
        return Result.success(userService.getCurrentUserByToken(token));
    }

    /**
     * 从 Authorization 头提取 Token
     * 约定格式：Authorization: Bearer <token>
     */
    private String extractToken(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return auth.substring(7); // 去掉 "Bearer " 前缀
    }
}
