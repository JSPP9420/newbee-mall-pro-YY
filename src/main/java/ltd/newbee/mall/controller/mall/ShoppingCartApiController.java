package ltd.newbee.mall.controller.mall;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ltd.newbee.mall.controller.dto.ShoppingCartItemSaveDTO;
import ltd.newbee.mall.controller.vo.CartVO;
import ltd.newbee.mall.service.NewBeeMallShoppingCartService;
import ltd.newbee.mall.util.Result;
import ltd.newbee.mall.util.ResultGenerator;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 购物车 REST API（pro 版新增，对应页面版 ShoppingCartController，测试通过后页面版移除）
 *
 * 设计决策：
 * - 路径挂在 /api/v1/mall/cart/** 下，自动被 MallUserTokenInterceptor（/api/v1/mall/**）
 *   拦截做 Token 认证，无需新增拦截器配置
 * - userId 从拦截器写入的 request attribute 获取，不碰 Session（无状态）
 * - 与页面版不同点：原 Controller 只做单操作，购物车角标/总价靠页面另行计算；
 *   REST 版写操作直接返回最新 CartVO，前端一次请求即可刷新角标与列表
 * - 业务失败由 Service 抛 NewBeeMallException，经全局异常处理器统一返回 JSON
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mall/cart")
public class ShoppingCartApiController {

    private final NewBeeMallShoppingCartService newBeeMallShoppingCartService;
    /**
     * 查询当前用户购物车（明细 + 总件数 + 总价聚合）
     */
    @GetMapping
    public Result<CartVO> getCart(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ResultGenerator.genSuccessResult(newBeeMallShoppingCartService.getMyCart(userId));
    }

    /**
     * 添加购物项（该商品已在购物车时等价于修改数量），返回最新购物车
     */
    @PostMapping
    public Result<CartVO> addItem(@Valid @RequestBody ShoppingCartItemSaveDTO cartItem,
                                  HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        newBeeMallShoppingCartService.saveNewBeeMallCartItem(userId, cartItem);
        // 走到这里即添加成功，返回最新购物车便于前端刷新角标与列表
        return ResultGenerator.genSuccessResult(newBeeMallShoppingCartService.getMyCart(userId));
    }

    /**
     * 修改购物项数量，返回最新购物车
     */
    @PutMapping
    public Result<CartVO> updateItem(@Valid @RequestBody ShoppingCartItemSaveDTO cartItem,
                                     HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        newBeeMallShoppingCartService.updateNewBeeMallCartItem(userId, cartItem);
        return ResultGenerator.genSuccessResult(newBeeMallShoppingCartService.getMyCart(userId));
    }

    /**
     * 删除购物项（SQL 带 userId 条件，防越权删除他人购物项），返回最新购物车
     */
    @DeleteMapping("/{cartItemId}")
    public Result<CartVO> deleteItem(@PathVariable("cartItemId") Long cartItemId,
                                     HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        newBeeMallShoppingCartService.deleteById(cartItemId, userId);
        return ResultGenerator.genSuccessResult(newBeeMallShoppingCartService.getMyCart(userId));
    }
}
