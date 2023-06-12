package com.atguigu.gulimall.order.controller;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.UUID;

/**
 * @Author:luosheng
 * @Date:2023-06-02 17:19
 * @Description:
 */
@Log4j2
@RestController
public class RabbitController {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @GetMapping("/sendMq")
    public String sendMq(@RequestParam(value = "num", defaultValue = "10") Integer num) {
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                //        String msg="Hello World!";
                //还可以直接发送一个对象 ,就要使用 序列化机制，将对象写出去，要求，对象必须实现 Serializable 这个接口
                OrderReturnReasonEntity orderReturnApplyEntity = new OrderReturnReasonEntity();
                orderReturnApplyEntity.setId(1L);
                orderReturnApplyEntity.setCreateTime(new Date());
                orderReturnApplyEntity.setName("想要退货" + i);

                //1.发送消息
                rabbitTemplate.convertAndSend("hello-java-exchange",
                        "hello.java", orderReturnApplyEntity);
                log.info("消息发送完成{}", orderReturnApplyEntity.toString());
            } else {
                OrderEntity orderEntity = new OrderEntity();
                orderEntity.setOrderSn(UUID.randomUUID().toString());
                //1.发送消息
                rabbitTemplate.convertAndSend("hello-java-exchange",
                        "hello.javaaaaaaaaa", orderEntity);
                log.info("消息发送完成{}", orderEntity.toString());
            }


        }

        return "ok";
    }
}
