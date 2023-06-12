package com.atguigu.gulimall.seckill.to;

import com.atguigu.gulimall.seckill.vo.SkuInfoVo;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author:luosheng
 * @Date:2023-06-08 8:03
 * @Description:
 */
@Data
public class SecKillSkuRedisTo {
    private Long id;
    /**
     * 活动id
     */
    private Long promotionId;
    /**
     * 活动场次id
     */
    private Long promotionSessionId;
    /**
     * 商品id
     */
    private Long skuId;

    /**
     * 商品的随机码
     */
    private String randomCode;
    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;
    /**
     * 秒杀总量
     */
    private BigDecimal seckillCount;
    /**
     * 每人限购数量
     */
    private Integer seckillLimit;
    /**
     * 排序
     */
    private Integer seckillSort;
    //SKU的详细信息
    private SkuInfoVo skuInfoVo;
    //开始结束时间 (为了好比较转成了long类型)
    //当前商品秒杀的开始时间
    private Long startTime;
    //当前商品秒杀的结束时间
    private Long endTime;
}
