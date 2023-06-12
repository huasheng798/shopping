package com.atguigu.gulimall.order.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;

/**
 * @Author:luosheng
 * @Date:2023-06-02 11:07
 * @Description:
 */
@Configuration
public class MyRabbitConfig {

    private    RabbitTemplate rabbitTemplate;


    //解决注入rabbitTemplate出现循环依赖问题
    @Primary   //将此bean的优先级提高
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        this.rabbitTemplate = rabbitTemplate;
        rabbitTemplate.setMessageConverter(messageConverter());
        initRabbitTemplate();
        return rabbitTemplate;
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
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
//    @PostConstruct //MyRabbitConfig对象创建完成以后,才来执行这个方法
    public void initRabbitTemplate() {

        //设置确认回调

        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             *
             * @param correlationData 当前消息的唯一关联数据(这个是消息的唯一id)
             * @param ack  消息是否成功收到
             * @param cause   失败的原因
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                /**
                 * 1.做好消息确认机制(pulisher,consumer【手动ack】)
                 * 2.每一个 发送的消息都在数据库做好记录。定期失败的消息在重新发送一遍
                 */
                System.out.println("confirm....correlationData[" + correlationData + "]==>ack[" + ack + "]==>" +
                        "cause[" + cause + "]");
            }
        });

        //设置消息抵达队列的确认回调
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            /**
             * 只要消息没有投递指定的队列，就会触发这个失败回调
             * @param message  投递失败的消息详细信息
             * @param replyCode   回复的状态码
             * @param replyText   回复的文本内容
             * @param exchange   当时这个消息发给那个交换机
             * @param routingKey   当时这个消息用那个路由键
             */
            @Override
            public void returnedMessage(Message message, int replyCode,
                                        String replyText, String exchange, String routingKey) {
                System.out.println("message[" + message + "]===replyCode[" + replyCode + "]===replyText" +
                        replyText + "]===exchange[" + exchange + "]===routingKey" + routingKey);
            }
        });
    }
}
