package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author:luosheng
 * @Date:2023-06-04 16:49
 * @Description:
 */
@Data
public class FareVo {
    private MemberAddressVo address;//地址
    private BigDecimal fare;//运费
}
