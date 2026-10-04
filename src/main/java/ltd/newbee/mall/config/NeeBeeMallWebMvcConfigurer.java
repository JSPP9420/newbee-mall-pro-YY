/**
 * 严肃声明：
 * 开源版本请务必保留此注释头信息，若删除我方将保留所有法律责任追究！
 * 本系统已申请软件著作权，受国家版权局知识产权以及国家计算机软件著作权保护！
 * 可正常分享和学习源码，不得用于违法犯罪活动，违者必究！
 * Copyright (c) 2019-2020 十三 all rights reserved.
 * 版权所有，侵权必究！
 */
package ltd.newbee.mall.config;

import ltd.newbee.mall.common.Constants;
import ltd.newbee.mall.mapper.AdminPermissionMapper;
import ltd.newbee.mall.interceptor.AdminLoginInterceptor;
import ltd.newbee.mall.interceptor.AdminPermissionInterceptor;
import ltd.newbee.mall.interceptor.AdminTokenInterceptor;
import ltd.newbee.mall.interceptor.MallUserTokenInterceptor;
import ltd.newbee.mall.interceptor.NewBeeMallCartNumberInterceptor;
import ltd.newbee.mall.interceptor.NewBeeMallLoginInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class NeeBeeMallWebMvcConfigurer implements WebMvcConfigurer {

    private final AdminLoginInterceptor adminLoginInterceptor;
    private final NewBeeMallLoginInterceptor newBeeMallLoginInterceptor;
    private final NewBeeMallCartNumberInterceptor newBeeMallCartNumberInterceptor;
    private final AdminTokenInterceptor adminTokenInterceptor;
    private final MallUserTokenInterceptor mallUserTokenInterceptor;
    private final ObjectProvider<AdminPermissionInterceptor> adminPermissionInterceptorProvider;

    public NeeBeeMallWebMvcConfigurer(
            AdminLoginInterceptor adminLoginInterceptor,
            NewBeeMallLoginInterceptor newBeeMallLoginInterceptor,
            NewBeeMallCartNumberInterceptor newBeeMallCartNumberInterceptor,
            AdminTokenInterceptor adminTokenInterceptor,
            MallUserTokenInterceptor mallUserTokenInterceptor,
            ObjectProvider<AdminPermissionInterceptor> adminPermissionInterceptorProvider) {
        this.adminLoginInterceptor = adminLoginInterceptor;
        this.newBeeMallLoginInterceptor = newBeeMallLoginInterceptor;
        this.newBeeMallCartNumberInterceptor = newBeeMallCartNumberInterceptor;
        this.adminTokenInterceptor = adminTokenInterceptor;
        this.mallUserTokenInterceptor = mallUserTokenInterceptor;
        this.adminPermissionInterceptorProvider = adminPermissionInterceptorProvider;
    }

    @Bean
    @ConditionalOnProperty(name = "newbee.mall.rbac.enabled", havingValue = "true")
    public AdminPermissionInterceptor adminPermissionInterceptor(
            AdminPermissionMapper adminPermissionMapper) {
        return new AdminPermissionInterceptor(adminPermissionMapper);
    }

    public void addInterceptors(InterceptorRegistry registry) {
        // pro 版新增：管理端 REST API 的 Token 认证（/api/v1/admin/**，放行登录接口）
        registry.addInterceptor(adminTokenInterceptor)
                .addPathPatterns("/api/v1/admin/**")
                .excludePathPatterns("/api/v1/admin/login");
        // pro 版新增：用户端 REST API 的 Token 认证（/api/v1/mall/**，放行登录/注册接口）
        registry.addInterceptor(mallUserTokenInterceptor)
                .addPathPatterns("/api/v1/mall/**")
                .excludePathPatterns("/api/v1/mall/login")
                .excludePathPatterns("/api/v1/mall/register");
        // 添加一个拦截器，拦截以/admin为前缀的url路径（后台登陆拦截）
        registry.addInterceptor(adminLoginInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login")
                .excludePathPatterns("/admin/dist/**")
                .excludePathPatterns("/admin/plugins/**");
        AdminPermissionInterceptor adminPermissionInterceptor =
                adminPermissionInterceptorProvider.getIfAvailable();
        if (adminPermissionInterceptor != null) {
            registry.addInterceptor(adminPermissionInterceptor)
                    .addPathPatterns("/admin/**")
                    .excludePathPatterns("/admin/login")
                    .excludePathPatterns("/admin/dist/**")
                    .excludePathPatterns("/admin/plugins/**");
        }
        // 购物车中的数量统一处理
        registry.addInterceptor(newBeeMallCartNumberInterceptor)
                .excludePathPatterns("/admin/**")
                .excludePathPatterns("/register")
                .excludePathPatterns("/login")
                .excludePathPatterns("/logout");
        // 商城页面登陆拦截（购物车已 REST 化为 /api/v1/mall/cart，走 Token 拦截，此处移除 /shop-cart 配置）
        registry.addInterceptor(newBeeMallLoginInterceptor)
                .excludePathPatterns("/admin/**")
                .excludePathPatterns("/register")
                .excludePathPatterns("/login")
                .excludePathPatterns("/logout")
                .addPathPatterns("/goods/detail/**")
                .addPathPatterns("/saveOrder")
                .addPathPatterns("/orders")
                .addPathPatterns("/orders/**")            
                .addPathPatterns("/personal")
                .addPathPatterns("/personal/updateInfo")
                .addPathPatterns("/selectPayType")
                .addPathPatterns("/payPage")
                .addPathPatterns("/paySuccess");
    }

    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/upload/**").addResourceLocations("file:" + Constants.FILE_UPLOAD_DIC);
        registry.addResourceHandler("/goods-img/**").addResourceLocations("file:" + Constants.FILE_UPLOAD_DIC);
    }
}
