package com.atguigu.gulimall.order.vo;

import com.atguigu.gulimall.order.entity.OrderEntity;
import lombok.Data;

/**
 * @Author:luosheng
 * @Date:2023-06-04 15:54
 * @Description:
 */
@Data
public class SubmitOrderResponseVo {
    private OrderEntity order;
    private Integer code;//错误状态 0 表示成功 ，其他则全是各种错误
}
