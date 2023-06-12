package com.atguigu.gulimall.order.web;

import com.atguigu.gulimall.order.entity.OrderEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.UUID;

/**
 * @Author:luosheng
 * @Date:2023-06-03 11:12
 * @Description:
 */
@Controller
public class HelloController {
    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * 模拟订单成功
     *
     * @return
     */
    @GetMapping("/test/createOrder")
    @ResponseBody
    public String createOrderTest() {
        //订单下单成功
        OrderEntity entity = new OrderEntity();
        entity.setOrderSn(UUID.randomUUID().toString());
        entity.setModifyTime(new Date());
        //给MQ发送消息
        //第一个参数为交换机，第二个为routingKey，第三个为发送的消息
        rabbitTemplate.convertAndSend("order-event-exchange",
                "order.create.order",
                entity);
        return "ok";
    }

    @GetMapping("/{page}.html")
    public String listPage(@PathVariable("page") String page) {
        return page;
    }

}
