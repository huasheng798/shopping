package com.atguigu.gulimall.order.config;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author:luosheng
 * @Date:2023-06-06 7:50
 * @Description:
 */
@Configuration
public class MyMQConfig {


    //@Bean Binding,Queue,

    /**
     * 容器中的 Binding,Queue,Exchange 都会自动创建(RabbitMQ没有的情况)
     * RabbitMQ 只要有。@Bean声明属性发生变化也不会覆盖
     *
     * @return
     */
    @Bean
    public Queue orderDelayQueue() {
        //要把它定为死信队列 需要添加属性
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "order-event-exchange");
        arguments.put("x-dead-letter-routing-key", "order.release.order");//routingKey
        arguments.put("x-message-ttl", 60000);//这里时以毫秒为单位，这里就拿一分钟测试一下，可能正常的业务需要等待半个小时未支付才会进入死信
        Queue queue = new Queue("order.delay.queue",
                true, false, false, arguments);
        return queue;
    }

    @Bean
    public Queue orderReleaseOrderQueue() {
        //普通队列
        Queue queue = new Queue("order.release.order.queue",
                true, false, false);
        return queue;
    }

    @Bean
    public Exchange orderEventExchange() {
        //创建交换机
        return new TopicExchange("order-event-exchange", true, false);
    }

    @Bean
    public Binding orderCreateOrderBinding() {
        //给交换机绑定 binding关系
        //我们和order.delay.queue 队列绑定
        //绑定类型为queue 队列
        //交换机为 order-event-exchange
        //绑定的routingKey 为 order.create.order
        return new Binding("order.delay.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.create.order",
                null);
    }

    @Bean
    public Binding orderReleaseOrderBinding() {
        return new Binding("order.release.order.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.order",
                null);
    }

    /**
     * 订单释放直接和库存释放进行绑定
     *
     * @return
     */
    @Bean
    public Binding orderReleaseOtherBinding() {
        return new Binding("stock.release.stock.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.other.#",
                null);
    }

    @Bean
    public Queue orderSeckillOrderQueue() {
        return new Queue("order.seckill.order.queue", true, false, false);
    }

    @Bean
    public Binding orderSeckillOrderQueueBinding() {
        return new Binding("order.seckill.order.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.seckill.order",
                null);
    }
}
