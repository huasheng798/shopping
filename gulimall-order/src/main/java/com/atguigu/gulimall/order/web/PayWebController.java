package com.atguigu.gulimall.order.web;

import com.alipay.api.AlipayApiException;
import com.atguigu.gulimall.order.config.AlipayTemplate;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.PayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Author:luosheng
 * @Date:2023-06-07 8:21
 * @Description:
 */
@Controller
public class PayWebController {


    @Autowired
    AlipayTemplate alipayTemplate;

    @Autowired
    OrderService orderService;
    @ResponseBody
    @GetMapping("/payOrder")
    public String payOrder(@RequestParam("orderSn") String orderSn) throws AlipayApiException {
//        PayVo payVo = new PayVo();
//        payVo.setBody();//订单的备注
//        payVo.setOut_trade_no();//订单号
//        payVo.setSubject();//订单的主题
//        payVo.setTotal_amount();
        PayVo payVo = orderService.getOrderPay(orderSn);
        //这个pay里面响应的是一个表单，并且里面还有可默认提交事件，所以我们直接返回pay，会进行跳转
        String pay = alipayTemplate.pay(payVo);
        System.out.println(pay);
        return pay;
    }

}
