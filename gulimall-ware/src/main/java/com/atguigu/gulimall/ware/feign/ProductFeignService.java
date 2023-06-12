package com.atguigu.gulimall.ware.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author:luosheng
 * @Date:2023-05-14 9:54
 * @Description:
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {

    /**
     * fen的两种写法
     * 经过和不经过网关的
     * /product/skuinfo/info/{skuId}
     * /api/producct/skuinfo/info/{skuId}
     * 1) .让所有请求过网关
     * 1.@FeignClient("gulimall-gateway"): 给gulimall-geteway 所在的及其发请求
     * 2) .直接让后台指定服务处理
     *      1、@FeignClient("gulimall-gateway")
     *       2./product/skuinfo/info/{skuId}
     * @param id
     * @return
     */
    @RequestMapping("/product/spuinfo/info/{id}")
    // @RequiresPermissions("product:spuinfo:info")
    R info(@PathVariable("id") Long id);
}
