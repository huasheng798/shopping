package com.atguigu.gulimall.cart.controller;

import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.vo.Cart;
import com.atguigu.gulimall.cart.vo.CartItem;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @Author:luosheng
 * @Date:2023-05-30 21:14
 * @Description:
 */
@Controller
public class CartController {

    @Autowired
    CartService cartService;

    @GetMapping("/currentUserCartItems")
    @ResponseBody
    public List<CartItem> getCurrentUserCartItems() {
        return cartService.getUserCartItems();
    }

    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId) {

        cartService.deleteItem(skuId);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num) {
        cartService.changeItemCount(skuId, num);
        return "redirect:http://cart.gulimall.com/cart.html";
    }


    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId,
                            @RequestParam("check") Integer check) {

        cartService.checkItem(skuId, check);
        return "redirect:http://cart.gulimall.com/cart.html";
    }


    /**
     * 浏览器有一个cookie; user-key; 表示用户身份，一个月后过期
     * 如果第一次使用jd的购物车功能，都会给一个临时的用户身份；
     * 浏览器以后保存，每次访问都会带上这个cookie；
     * <p>
     * 登录:session有
     * 没登录:按照cookie里面带来的user-key来做。
     * 第一次:如果没有理论你是用户，帮忙创建一个临时用户
     *
     * @param
     * @return
     */
    @GetMapping("/cart.html")
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {

        //1.快速得到用户信息，id，user-key
//        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
//        System.out.println(userInfoTo);
        Cart cart = cartService.getCart();
        model.addAttribute("cart", cart);
        return "cartList";
    }

    /**
     * 添加商品到购物车
     * RedirectAttributes re
     * ra.addFlashAttribute(); 他是将数据放在session里面可以在页面取出，但是只能取一次
     * ra.addAttribute();//他是将数据放在url后面
     *
     * @return //首先添加购物信息的时候必备，skuId(商品的id)和num(购买商品的数量)
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num,
                            RedirectAttributes re) throws ExecutionException, InterruptedException {
        cartService.addToCart(skuId, num);
        re.addAttribute("skuId", skuId);
        return "redirect:http://cart.gulimall.com/addToCartSuccess.html";
    }

    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId, Model model) {
        //重定向到成功页面。再次查询购物车数据即可
        CartItem item = cartService.getCartItem(skuId);
        model.addAttribute("item", item);
        return "success";
    }

}
