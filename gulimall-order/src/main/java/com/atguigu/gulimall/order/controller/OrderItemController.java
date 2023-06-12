package com.atguigu.gulimall.order.controller;

import java.util.Arrays;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.alibaba.fastjson.JSON;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.service.OrderItemService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;


/**
 * 订单项信息
 *
 * @author luosheng
 * @email luosheng@gmail.com
 * @date 2023-05-02 21:06:06
 */
@RabbitListener(queues = {"hello-java-queue"})
@RestController
@RequestMapping("order/orderitem")

public class OrderItemController {
    @Autowired
    private OrderItemService orderItemService;


    /**
     * queues：声明需要监听的所有队列
     * <p>
     * org.springframework.amqp.core.Message
     * <p>
     * 参数可以写以下类型
     * 1.Message message: 原生消息详细信息。头+体
     * 2.T <发送的消息的类型>OrderReturnReasonEntity content 。(这个获取来的直接是个消息的实体类类型)
     * 3.Channel channel:当前传输数据的通道
     * <p>
     * Queue: 可以多人都来监听。只要收到消息，队列删除消息，而且只能有以恶收到此消息.
     * 场景:
     * 1)、订单服务启动多个;同一个消息，只能有一个客户端收到
     * 2)、只有一个消息完全处理完，方法运行结束，我们就可以接收到下一个消息
     *
     */
//    @RabbitListener(queues = {"hello-java-queue"})  这个可以标注在类上 然后使用@RabbitHandler 注解标注具体的那个方法
    @RabbitHandler
    public void recieveMessage(Message message, OrderReturnReasonEntity content, Channel channel) {
        //{"id":1,"name":"想要退货","sort":null,"status":null,"createTime":1685684920138}
        System.out.println("接受到消息...");
        byte[] body = message.getBody();
        //消息头属性信息
        MessageProperties properties = message.getMessageProperties();
        System.out.println("接受到消息..." + message + "==>内容" + content);
        //这个是一个自增的，是在channel(通道内自增的一个数字)  每收到一个消息就增1
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        System.out.println("deliveryTag" + deliveryTag);
        //签收货物   第二个参数，是否批量签收货物，现在是非批量模式

        try {
            //他还有第三个参数requeue=false 丢弃，requeue=true 发回服务器，服务器重新入队
            channel.basicAck(deliveryTag, false);


        } catch (Exception e) {
            //能出现这个异常，可能就是网络中断
        }

    }

    @RabbitHandler
    public void recieveMessage(OrderEntity order) {
        System.out.println("接受到消息..." + order);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("order:orderitem:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = orderItemService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("order:orderitem:info")
    public R info(@PathVariable("id") Long id) {
        OrderItemEntity orderItem = orderItemService.getById(id);

        return R.ok().put("orderItem", orderItem);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("order:orderitem:save")
    public R save(@RequestBody OrderItemEntity orderItem) {
        orderItemService.save(orderItem);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("order:orderitem:update")
    public R update(@RequestBody OrderItemEntity orderItem) {
        orderItemService.updateById(orderItem);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("order:orderitem:delete")
    public R delete(@RequestBody Long[] ids) {
        orderItemService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
