package com.atguigu.gulimall.ware.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author:luosheng
 * @Date:2023-06-02 11:07
 * @Description: 我们如果想让服务启动，就直接创建 我们@Bean创建的内容，就必须让这个服务随便监听个队列，它这里应该是个懒加载的机制
 * @RabbitListener(queues = "stock.release.stock.queue")
 */
@Configuration
public class MyRabbitConfig {
//写这个会导致出现循环依赖问题
//    @Autowired
//    RabbitTemplate rabbitTemplate;

    /**
     * JSON转换
     *
     * @return
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    //随便监听一个队列  （本来随便写的一个消息有的被他消费还，）
//    @RabbitListener(queues = "stock.release.stock.queue")
//    public void handle(Message message) {
//        System.out.println(message+"啊啊啊啊啊啊啊啊啊啊啊啊");
//    }

    //创建个交换机 主题模式
    @Bean
    public Exchange stockEvenExchange() {
        return new TopicExchange("stock-event-exchange",
                true, false);
    }


    //创建普通队列
    @Bean
    public Queue stockReleaseStockQueue() {
        //是否持久化，true，是否单个连接，false，多人都额可以连接，是否自动删除false
        return new Queue("stock.release.stock.queue", true, false, false);
    }

    //创建延迟队列
    @Bean
    public Queue stockDelayQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", "stock-event-exchange");//信死了也先加入库存交换机
        args.put("x-dead-letter-routing-key", "stock.release");
        args.put("x-message-ttl", 30000);
        return new Queue("stock.delay.queue",
                true,
                false,
                false,
                null);
    }

    @Bean
    public Binding stockReleaseBinding() {
        return new Binding("stock.release.stock.queue",
                Binding.DestinationType.QUEUE
                ,"stock-event-exchange",
                "stock.release.#",
                null);
    }

    @Bean
    public Binding stockLockedBinding() {
        return new Binding("stock.delay.queue",
                Binding.DestinationType.QUEUE
                , "stock-event-exchange",

                "stock.locked",
                null);
    }

    /**
     * 定制RabbitTemplate
     * 1.spring.rabbitmq.publisher-confirms=true
     * 2.设置确认回调
     * 消息正确抵达队列进行回调
     * <p>
     * 1.spring.rabbitmq.publisher-returns=true
     * spring.rabbitmq.template.mandatory=true
     * 2.设置确认回调ReturnCallback
     * <p>
     * 3.消费端确认(保证每个消息被正确消费，此时才可以broker删除这个消息)
     * spring.rabbitmq.listener.simple.acknowledge-mode=manual  手动签收配置
     * 1.默认是自动确认的，只要消息接受到，客户端会自动确认，服务端就会移除这个消息
     * 这个问题(消息丢失)
     * 我们收到了很多消息，自动回复给服务器，ack，只有一个消息处理成功了，这时候宕机了。(还有消息没有消费)发生了消息的丢失
     * 手动确认模式，只要我们没有明确告诉MQ，货物被签收。没有Ack，消息就一直是unackedj状态.
     *  即使Consumer宕机(消费端宕机) 消息也不会丢失，会重新变为Ready 下一次有新的Consumer连接进来就会发给他
     *  2.如何签收
     *       channel.basicAck(deliveryTag,false); 签收，业务成功完成就应该签收
     *       channel.basicNack(deliveryTag,false,true); 拒签；业务失败，拒签
     */
/*    @PostConstruct //MyRabbitConfig对象创建完成以后,才来执行这个方法
    public void initRabbitTemplate() {

        //设置确认回调

        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            *//**
     *
     * @param correlationData 当前消息的唯一关联数据(这个是消息的唯一id)
     * @param ack  消息是否成功收到
     * @param cause   失败的原因
     *//*
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                System.out.println("confirm....correlationData[" + correlationData + "]==>ack[" + ack + "]==>" +
                        "cause[" + cause + "]");
            }
        });

        //设置消息抵达队列的确认回调
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            *//**
     * 只要消息没有投递指定的队列，就会触发这个失败回调
     * @param message  投递失败的消息详细信息
     * @param replyCode   回复的状态码
     * @param replyText   回复的文本内容
     * @param exchange   当时这个消息发给那个交换机
     * @param routingKey   当时这个消息用那个路由键
     *//*
            @Override
            public void returnedMessage(Message message, int replyCode,
                                        String replyText, String exchange, String routingKey) {
                System.out.println("message[" + message + "]===replyCode[" + replyCode + "]===replyText" +
                        replyText + "]===exchange[" + exchange + "]===routingKey" + routingKey);
            }
        });
    }*/
}
