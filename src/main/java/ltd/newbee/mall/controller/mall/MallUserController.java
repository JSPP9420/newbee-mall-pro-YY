/**
 * 严肃声明：
 * 开源版本请务必保留此注释头信息，若删除我方将保留所有法律责任追究！
 * 本系统已申请软件著作权，受国家版权局知识产权以及国家计算机软件著作权保护！
 * 可正常分享和学习源码，不得用于违法犯罪活动，违者必究！
 * Copyright (c) 2019-2020 十三 all rights reserved.
 * 版权所有，侵权必究！
 */
package ltd.newbee.mall.controller.mall;

import cn.hutool.captcha.ShearCaptcha;
import ltd.newbee.mall.common.Constants;
import ltd.newbee.mall.common.ServiceResultEnum;
import ltd.newbee.mall.controller.dto.MallUserUpdateDTO;
import ltd.newbee.mall.controller.vo.MallUserVO;
import ltd.newbee.mall.security.CsrfTokenService;
import ltd.newbee.mall.service.MallUserService;
import ltd.newbee.mall.util.Result;
import ltd.newbee.mall.util.ResultGenerator;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class MallUserController {

    private final MallUserService mallUserService;

    public MallUserController(MallUserService newBeeMallUserService) {
        this.mallUserService = newBeeMallUserService;
    }

    @GetMapping("/personal")
    public String personalPage(HttpServletRequest request,
                               HttpSession httpSession) {
        request.setAttribute("path", "personal");
        return "mall/personal";
    }

    @PostMapping("/logout")
    public String logout(HttpSession httpSession) {
        httpSession.removeAttribute(Constants.MALL_USER_SESSION_KEY);
        return "mall/login";
    }

    @GetMapping({"/login", "login.html"})
    public String loginPage() {
        return "mall/login";
    }

    @GetMapping({"/register", "register.html"})
    public String registerPage() {
        return "mall/register";
    }

    @GetMapping("/personal/addresses")
    public String addressesPage() {
        return "mall/addresses";
    }

    @PostMapping("/login")
    @ResponseBody
    public Result login(@RequestParam("loginName") String loginName,
                        @RequestParam("verifyCode") String verifyCode,
                        @RequestParam("password") String password,
                        HttpServletRequest request,
                        HttpSession httpSession) {
        if (!StringUtils.hasText(loginName)) {
            return ResultGenerator.genFailResult(ServiceResultEnum.LOGIN_NAME_NULL.getResult());
        }
        if (!StringUtils.hasText(password)) {
            return ResultGenerator.genFailResult(ServiceResultEnum.LOGIN_PASSWORD_NULL.getResult());
        }
        if (!StringUtils.hasText(verifyCode)) {
            return ResultGenerator.genFailResult(ServiceResultEnum.LOGIN_VERIFY_CODE_NULL.getResult());
        }
        ShearCaptcha shearCaptcha = (ShearCaptcha) httpSession.getAttribute(Constants.MALL_VERIFY_CODE_KEY);

        if (shearCaptcha == null || !shearCaptcha.verify(verifyCode)) {
            return ResultGenerator.genFailResult(ServiceResultEnum.LOGIN_VERIFY_CODE_ERROR.getResult());
        }
        // 失败由 Service 抛 NewBeeMallException，经全局异常处理器返回 JSON
        MallUserVO loginUser = mallUserService.login(loginName, password);
        request.changeSessionId();
        httpSession.setAttribute(Constants.MALL_USER_SESSION_KEY, loginUser);
        httpSession.removeAttribute(Constants.MALL_VERIFY_CODE_KEY);
        CsrfTokenService.rotateToken(httpSession);
        return ResultGenerator.genSuccessResult();
    }

    @PostMapping("/register")
    @ResponseBody
    public Result register(@RequestParam("loginName") String loginName,
                           @RequestParam("verifyCode") String verifyCode,
                           @RequestParam("password") String password,
                           HttpSession httpSession) {
        if (!StringUtils.hasText(loginName)) {
            return ResultGenerator.genFailResult(ServiceResultEnum.LOGIN_NAME_NULL.getResult());
        }
        if (!StringUtils.hasText(password)) {
            return ResultGenerator.genFailResult(ServiceResultEnum.LOGIN_PASSWORD_NULL.getResult());
        }
        if (!StringUtils.hasText(verifyCode)) {
            return ResultGenerator.genFailResult(ServiceResultEnum.LOGIN_VERIFY_CODE_NULL.getResult());
        }
        ShearCaptcha shearCaptcha = (ShearCaptcha) httpSession.getAttribute(Constants.MALL_VERIFY_CODE_KEY);
        if (shearCaptcha == null || !shearCaptcha.verify(verifyCode)) {
            return ResultGenerator.genFailResult(ServiceResultEnum.LOGIN_VERIFY_CODE_ERROR.getResult());
        }
        // 失败由 Service 抛 NewBeeMallException，经全局异常处理器返回 JSON
        mallUserService.register(loginName, password);
        //注册成功，删除session中的verifyCode
        httpSession.removeAttribute(Constants.MALL_VERIFY_CODE_KEY);
        return ResultGenerator.genSuccessResult();
    }

    @PostMapping("/personal/updateInfo")
    @ResponseBody
    public Result updateInfo(@Valid @RequestBody MallUserUpdateDTO updateDTO, HttpSession httpSession) {
        if (updateDTO == null) {
            return ResultGenerator.genFailResult("参数异常！");
        }
        MallUserVO currentUser = (MallUserVO) httpSession
                .getAttribute(Constants.MALL_USER_SESSION_KEY);
        if (currentUser == null || currentUser.getUserId() == null) {
            return ResultGenerator.genFailResult("请先登录");
        }
        MallUserVO updatedUser = mallUserService
                .updateUserInfo(currentUser.getUserId(), updateDTO);
        if (updatedUser == null) {
            return ResultGenerator.genFailResult("修改失败");
        }
        httpSession.setAttribute(Constants.MALL_USER_SESSION_KEY, updatedUser);
        return ResultGenerator.genSuccessResult();
    }
}
