package com.atguigu.gulimall.cart.service;

import com.atguigu.gulimall.cart.vo.Cart;
import com.atguigu.gulimall.cart.vo.CartItem;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @Author:luosheng
 * @Date:2023-05-30 21:06
 * @Description:
 */
public interface CartService {
    /**
     * 将商品添加到购物车
     *
     * @param skuId
     * @param num
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    /**
     * 获取购物车中某个购物项
     *
     * @param skuId
     * @return
     */
    CartItem getCartItem(Long skuId);

    /**
     * 获取整个购物车
     *
     * @return
     */
    Cart getCart() throws ExecutionException, InterruptedException;

    /**
     * 清空购物车数据
     *
     * @param cartkey
     */
    void clearCart(String cartkey);

    /**
     * 更改状态
     *
     * @param skuId
     * @param check
     */
    void checkItem(Long skuId, Integer check);

    /**
     * 更改数量
     *
     * @param skuId
     * @param num
     */
    void changeItemCount(Long skuId, Integer num);

    /**
     * 实现删除购物车的某个项
     * @param skuId
     */
    void deleteItem(Long skuId);

    List<CartItem> getUserCartItems();
}
