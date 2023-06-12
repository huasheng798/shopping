package com.atguigu.gulimall.order;


import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderReturnApplyEntity;
import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.UUID;

@Slf4j
@SpringBootTest
class GulimallOrderApplicationTests {

    @Autowired
    AmqpAdmin amqpAdmin;//用于创键队列 交换机 以及绑定关系

    @Autowired
    RabbitTemplate rabbitTemplate;//用于收发消息

    @Test
    public void sendMessageTest() {

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
                        "hello.java", orderEntity);
                log.info("消息发送完成{}", orderEntity.toString());
            }


        }

    }


    /**
     * 1.如何创建Exchange【hello-java-exchange】 以及Queue  和Binding关系
     * 1.使用AmqpAdmin进行创建
     * 2.如何收发消息
     */
    @Test
    public void createExchange() {
        //amqpAdmin
        /**
         * DirectExchange(String name, boolean durable, boolean autoDelete, Map<String, Object> arguments)
         *  name:第一个为交换机的名字
         *  durable:是否进行持久化
         *  autoDelete:是否自动删除 (我们直接使用false 默认的配置)
         *  arguments:这里可以设置一些map的参数，我们这里可以不使用
         *  上面这些参数在可视化界面中都可以看到
         */
        DirectExchange directExchange = new DirectExchange("hello-java-exchange", true,
                false);//Direct模式的交换机,
        amqpAdmin.declareExchange(directExchange);//声明一个交换机
        log.info("Exchange[{}]创建成功", directExchange.getName());
    }

    /**
     * Queue(String name, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments)
     * exclusive :排他队列，
     */
    @Test
    public void createQueue() {
        Queue queue = new Queue("hello-java-queue", true, false,
                false, null);
        amqpAdmin.declareQueue(queue);
        log.info("queue[{}]创建成功", queue.getName());
    }

    /**
     * (String destination【目的地】,
     * Binding.DestinationType destinationType【目的地的类型】,
     * String exchange【交换机】,
     * String routingKey【路由key】,
     * Map<String, Object> arguments【一些自定义参数】)
     * //将exchange指定的交换机和destination目的地继续宁绑定，使用routingKey作为路由键
     */
    @Test
    public void createBinding() {
        Binding binding = new Binding("hello-java-queue",
                Binding.DestinationType.QUEUE,
                "hello-java-exchange",
                "hello.java", null);
        amqpAdmin.declareBinding(binding);
        log.info("queue[{}]创建成功", "hello-java-binding");

    }

}
