package com.atguigu.gulimall.ware.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @Author:luosheng
 * @Date:2023-06-06 13:53
 * @Description:
 */
@FeignClient("gulimall-order")
public interface OrderFeignService {

    @GetMapping("/order/order/status/{orderSn}")
    public R getOrderStatus(@PathVariable("orderSn") String orderSn);
}
