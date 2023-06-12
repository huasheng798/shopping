package com.atguigu.gulimall.seckill.service;

import com.atguigu.gulimall.seckill.to.SecKillSkuRedisTo;

import java.util.List;

/**
 * @Author:luosheng
 * @Date:2023-06-07 22:05
 * @Description:
 */
public interface SeckillService {
    void uploadSeckillSkuLatest3Days();

    List<SecKillSkuRedisTo> getCurrentSeckillSkus();

    SecKillSkuRedisTo getSkuSeckillInfo(Long skuId);

    String kill(String killId, String key, Integer num) throws InterruptedException;
}
