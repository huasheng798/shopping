package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author:luosheng
 * @Date:2023-06-04 13:39
 * @Description:
 */
@Data
public class FareVo {
    private MemberAddressVo address;
    private BigDecimal fare;
}
