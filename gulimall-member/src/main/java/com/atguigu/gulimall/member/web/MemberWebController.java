package com.atguigu.gulimall.member.web;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.member.feign.OrderFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author:luosheng
 * @Date:2023-06-07 13:59
 * @Description:
 */
@Controller
public class MemberWebController {

    @Autowired
    OrderFeignService orderFeignService;

    @GetMapping("/memberOrder.html")
    public String memberOrderPage(@RequestParam(value = "pageNum", defaultValue = "1") String pageNum,
                                  Model model) {
        //获取到支付宝给我们传来的所有请求数据

        //查出当前登录的用户的所有订单列表数据
        HashMap<String, Object> page = new HashMap<>();
        page.put("page", pageNum);
        R r = orderFeignService.listWithItem(page);
        System.out.println(JSON.toJSONString(r));
        model.addAttribute("orders", r);
        return "orderList";
    }
}
