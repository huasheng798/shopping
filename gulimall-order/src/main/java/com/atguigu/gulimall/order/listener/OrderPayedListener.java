package com.atguigu.gulimall.order.listener;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @Author:luosheng
 * @Date:2023-06-07 20:30
 * @Description:
 */
@RestController
public class OrderPayedListener {

    @PostMapping("")
    public String handleAlipayed(HttpServletRequest request) {
        //只要我们收到了支付宝给我们异步的通知，告诉我们订单支付成功。返回success，支付宝就再也不通知
        Map<String, String[]> parameterMap = request.getParameterMap();
        System.out.println("支付宝通知到位了" + parameterMap);
        return "success";
    }
}
