package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author:luosheng
 * @Date:2023-06-05 7:49
 * @Description:
 */
@Data
public class WareSkuLockVo {
    private String orderSn;//订单号

    private List<OrderItemVo> locks;//需要锁住的所有库存信息


}
