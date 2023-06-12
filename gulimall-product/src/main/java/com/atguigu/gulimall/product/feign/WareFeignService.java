package com.atguigu.gulimall.product.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.vo.SkuHasStockVo;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @Author:luosheng
 * @Date:2023-05-16 11:30
 * @Description:
 */
@FeignClient("gulimall-ware")
public interface WareFeignService {

    /**
     * 返回结果
     * 1.R 设计的时候可以加上泛型
     * 2.直接返回我们想要的结果
     * 3.自己封装一个解析结果
     * @param skuIds
     * @return
     */
    @PostMapping("/ware/waresku/hasstock")
    R getSkusHasStock(@RequestBody List<Long> skuIds);
}
