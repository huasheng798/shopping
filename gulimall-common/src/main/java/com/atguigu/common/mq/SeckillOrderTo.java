package com.atguigu.common.mq;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author:luosheng
 * @Date:2023-06-09 9:58
 * @Description:
 */
@Data
public class SeckillOrderTo {
    private String orderSn;//订单号
    private Long promotionSessionId;//活动场次id
    private Long skuId;//商品id
    private BigDecimal seckillPrice;//秒杀价格
    private Integer num;//购买数量
    private Long memberId;//会员id


}
