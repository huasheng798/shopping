package com.atguigu.gulimall.ware.listener;

import com.atguigu.common.mq.OrderTo;
import com.atguigu.common.mq.StockLockedTo;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @Author:luosheng
 * @Date:2023-06-06 15:57
 * @Description:
 */
@Service
@RabbitListener(queues = "stock.release.stock.queue")//监听这个队列，如果这个队列中接受到了数据，说明要解锁，
public class StockReleaseListener {
    @Autowired
    WareSkuService wareSkuService;


    /**
     * 库存自动解锁
     * <p>
     * 第一种 解锁 情况 下单成功，库存锁定成功，接下来的业务调用失败，导致订单回滚，
     * 这时候就要之前锁定的库存就要自动解锁
     * 我们订单失败，就是锁定库存导致的
     * <p>
     * 只要解锁库存的消息失败。一定要告诉服务解锁失败
     *
     * @param to
     * @param message
     */
    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo to, Message message,
                                         Channel channel) throws IOException {
        try {
            wareSkuService.unlockStock(to);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

    @RabbitHandler
    public void handleOrderCloseRelease(OrderTo order, Message message, Channel channel) throws IOException {
        System.out.println("订单关闭准备解锁库存....");
        try {
            wareSkuService.unlockStock(order);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
}
