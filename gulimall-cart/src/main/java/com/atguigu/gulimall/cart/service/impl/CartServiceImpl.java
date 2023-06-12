package com.atguigu.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.CartConstant;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.cart.config.MyThreadConfig;
import com.atguigu.gulimall.cart.feign.ProductFeignService;
import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.vo.Cart;
import com.atguigu.gulimall.cart.vo.CartItem;
import com.atguigu.gulimall.cart.vo.SkuInfoVo;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @Author:luosheng
 * @Date:2023-05-30 21:06
 * @Description:
 */
@Slf4j
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    private final String CART_PREFIX = "gulimall:cart";

    @Autowired
    ThreadPoolExecutor executor;

    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        //2.商品添加到购物车

        String res = (String) cartOps.get(skuId.toString());
        if (StringUtils.isEmpty(res)) {

            //购物车无此商品
            CartItem cartItem = new CartItem();

            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                //1.远程查询当前要添加的商品信息
                R skuInfo = productFeignService.getSkuInfo(skuId);
                SkuInfoVo data = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });
                cartItem.setCheck(true);
                cartItem.setCount(num);
                cartItem.setImage(data.getSkuDefaultImg());
                cartItem.setTitle(data.getSkuTitle());
                cartItem.setSkuId(skuId);
                cartItem.setPrice(data.getPrice());
            }, executor);
            //远程查询当前要添加的商品信息

            //远程查询sku的组合信息
            CompletableFuture<Void> getSkuSaleAttrValues = CompletableFuture.runAsync(() -> {
                List<String> values = productFeignService.getSkuSaleAttrValues(skuId);
                cartItem.setSkuAttr(values);
            }, executor);
            //我们必须等待上面异步的线程执行完才能转换数据put数据
            CompletableFuture.allOf(getSkuSaleAttrValues, future).get();//等待上面两个任务执行完
            String jsonString = JSON.toJSONString(cartItem);
            cartOps.put(skuId.toString(), jsonString);
            return cartItem;
        } else {
            //购物车有此商品，修改数量
            CartItem cartItem = JSON.parseObject(res, CartItem.class);
            cartItem.setCount(cartItem.getCount() + num);

            cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
            return cartItem;
        }

    }

    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String str = (String) cartOps.get(skuId.toString());
        CartItem cartItem = JSON.parseObject(str, CartItem.class);
        return cartItem;
    }

    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {

        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        Cart cart = new Cart();

        if (userInfoTo.getUserId() != null) {
            //1.登录
            String cartKey = CART_PREFIX + userInfoTo.getUserId();
            //如果临时购物车的数据还没有进行合并
            String tempCartKey = CART_PREFIX + userInfoTo.getUserKey();
            List<CartItem> tempCartItems = getCartItems(tempCartKey);
            if (tempCartItems != null) {
                //说明临时的购物车有数据 需要进行合并操作
                //合并临时购物车操作
                for (CartItem tempCartItem : tempCartItems) {
                    addToCart(tempCartItem.getSkuId(), tempCartItem.getCount());//添加购物车操作

                }
                //在操作完要清空临时购物车
                clearCart(tempCartKey);
            }

            //3.获取登陆后的购物车的数据【包含合并过来的临时购物车的数据，和登陆后的购物车的数据】
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);
        } else {
            //不登录
            String cartKey = CART_PREFIX + userInfoTo.getUserKey();
            //获取临时购物车 的所有购物项
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);

        }

        return cart;
    }

    @Override
    public void clearCart(String cartkey) {
        redisTemplate.delete(cartkey);
    }

    @Override
    public void checkItem(Long skuId, Integer check) {
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCheck(check == 1);
        String jsonString = JSON.toJSONString(cartItem);

        //要往那个购物车中添加(以当前登录状态获取)
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.put(skuId.toString(), jsonString);
    }

    @Override
    public void changeItemCount(Long skuId, Integer num) {
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCount(num);

        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String jsonString = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(), jsonString);

    }

    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }

    @Override
    public List<CartItem> getUserCartItems() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        //先判断是否登录了
        if (userInfoTo.getUserId() == null) {
            return null;
        } else {
            //我们拿这个拼接的购物项key 就可以直接在reids中找到对应的购物车信息
            String cartKey = CART_PREFIX + userInfoTo.getUserId();
            List<CartItem> cartItems = getCartItems(cartKey);
            //但上面那个是所有的 我们只要选中的 也就是check这个属性要true
            if (cartItems == null) {
                return null;
            }
            List<CartItem> collect = cartItems.stream().
                    filter(cartItem -> cartItem.getCheck()).map(item -> {
                        //因为这里毕竟是从缓存中拿到的数据，价格万一变 了，所有这里要更新为最新的价格数据
                        R data = productFeignService.getPrice(item.getSkuId());
                        String price = (String) data.get("data");
                        item.setPrice(new BigDecimal(price));
                        return item;
                    }).
                    collect(Collectors.toList());
            return collect;
        }


    }

    /**
     * 获取我们要操作的购物车
     *
     * @return
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        //1.
        String cartKey = "";
        if (userInfoTo.getUserId() != null) {
            //差不多这样 gulimall:cart:1
            cartKey = CART_PREFIX + userInfoTo.getUserId();
        } else {
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
        }
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey);
        return operations;
    }

    /**
     * 获取购物车的所有购物项
     *
     * @param cartKey
     * @return
     */
    private List<CartItem> getCartItems(String cartKey) {
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(cartKey);
        List<Object> values = hashOps.values();
        if (values != null && values.size() > 0) {
            List<CartItem> collect = values.stream().map((obj) -> {
                String str = (String) obj;
                CartItem cartItem = JSON.parseObject(str, CartItem.class);
                return cartItem;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }
}
