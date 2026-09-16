package com.newbee.mall.controller;

import com.newbee.mall.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 健康检查接口
 * 验证：Spring Boot 启动 + 路由 + 统一返回 + Knife4j 文档 全链路通不通
 */
@Tag(name = "系统", description = "健康检查")
@RestController
@RequestMapping("/api/v1")
public class PingController {

    @Operation(summary = "Ping")
    @GetMapping("/ping")
    public Result<Map<String, String>> ping() {
        return Result.success(Map.of("status", "ok"));
    }
}
