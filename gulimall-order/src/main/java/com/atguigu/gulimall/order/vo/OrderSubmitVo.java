package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author:luosheng
 * @Date:2023-06-04 15:23
 * @Description:
 */

/**
 * 封装订单提交的数据
 */
@Data
public class OrderSubmitVo {
    private Long addrId;//收获地址的id
    private Integer payType;//支付方式
    //无需提交需要购买的商品，去购物车再获取一遍
    //优惠发票

    private String orderToken;//反重令牌
    private BigDecimal payPrice;//应付价格 验价
    private String note;//订单的备注（这个没有）
    //用户相关信息，直接去session中取出登录用户的信息


}
