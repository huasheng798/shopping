package com.atguigu.gulimall.seckill.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @Author:luosheng
 * @Date:2023-06-07 22:07
 * @Description:
 */
@FeignClient("gulimall-coupon")
public interface CouponFeignService {
    //获取最近三天的所有秒杀活动
    @GetMapping("/coupon/seckillsession/lates3DaySession")
     R getLates3DaySession();
}
