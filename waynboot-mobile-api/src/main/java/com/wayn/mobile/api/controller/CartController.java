package com.wayn.mobile.api.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.common.base.controller.BaseController;
import com.wayn.common.config.WaynConfig;
import com.wayn.common.util.R;
import com.wayn.mobile.api.domain.Cart;
import com.wayn.mobile.api.service.ICartService;
import com.wayn.mobile.framework.security.util.MobileSecurityUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车接口
 *
 * @author wayn
 * @since 2020-08-03
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("cart")
public class CartController extends BaseController {

    private ICartService iCartService;

    /**
     * 购物车列表
     *
     * @return
     */
    @GetMapping("list")
    public R list() {
        Long userId = MobileSecurityUtils.getUserId();
        Page<Cart> page = getPage();
        R list = iCartService.list(page, userId);
        return list;
    }

    /**
     * 添加购物车
     *
     * @return R
     */
    @PostMapping
    public R add(@RequestBody Cart cart) {
        return iCartService.add(cart);
    }

    /**
     * 往购物车中添加默认商品
     *
     * @return R
     */
    @PostMapping("addDefaultGoodsProduct")
    public R addDefaultGoodsProduct(@RequestBody Cart cart) {
        return iCartService.addDefaultGoodsProduct(cart);
    }

    /**
     * 更新购物车
     *
     * @param cart 更新参数
     * @return R
     */
    @PutMapping
    public R update(@RequestBody Cart cart) {
        return R.result(iCartService.updateById(cart));
    }

    /**
     * 修改购物车商品数量
     *
     * @param cartId 购物车id
     * @param number 更新数量
     * @return R
     */
    @PostMapping("changeNum/{cartId}/{number}")
    public R changeNum(@PathVariable Long cartId, @PathVariable Integer number) {
        return R.result(iCartService.changeNum(cartId, number));
    }

    /**
     * 删除购物车商品
     *
     * @param cartId 购物车id
     * @return R
     */
    @DeleteMapping("{cartId}")
    public R delete(@PathVariable Long cartId) {
        return R.result(iCartService.removeById(cartId));
    }

    /**
     * 统计购物车中现有商品数量
     *
     * @return R
     */
    @GetMapping("goodsCount")
    public R goodsCount() {
        return iCartService.goodsCount();
    }

    /**
     * 统计购物车中已勾选商品数量
     *
     * @return R
     */
    @PostMapping("getCheckedGoods")
    public R getCheckedGoods() {
        Long userId = MobileSecurityUtils.getUserId();
        List<Cart> cartList = iCartService.list(new QueryWrapper<Cart>()
                .eq("user_id", userId).eq("checked", true));
        BigDecimal goodsAmount = BigDecimal.ZERO;
        BigDecimal orderTotalAmount = BigDecimal.ZERO;
        // 计算总价
        for (Cart cart : cartList) {
            goodsAmount = goodsAmount.add(cart.getPrice().multiply(new BigDecimal(cart.getNumber())));
        }

        // 根据订单商品总价计算运费，满足条件（例如88元）则免运费，否则需要支付运费（例如8元）；
        BigDecimal freightPrice = BigDecimal.ZERO;
        if (goodsAmount.compareTo(WaynConfig.getFreightLimit()) < 0) {
            freightPrice = WaynConfig.getFreightPrice();
        }
        orderTotalAmount = goodsAmount.add(freightPrice);

        return R.success()
                .add("data", cartList)
                .add("freightPrice", freightPrice)
                .add("goodsAmount", goodsAmount)
                .add("orderTotalAmount", orderTotalAmount);
    }
}
