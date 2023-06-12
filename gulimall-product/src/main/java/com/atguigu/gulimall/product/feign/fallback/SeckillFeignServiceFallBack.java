package com.atguigu.gulimall.product.feign.fallback;

import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.feign.SeckillFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Author:luosheng
 * @Date:2023-06-11 10:47
 * @Description:
 */
@Slf4j
@Component
public class SeckillFeignServiceFallBack implements SeckillFeignService {
    //这个就是我们在其他feing调用其他服务的时候，如果出现问题，会调用此接口，前提要加fallback=SeckillFeignServiceFallBack/class
    @Override
    public R getSkuSeckillInfo(Long skuId) {
        log.error("熔断方法调用....getSkuSeckillInfo");
        return R.error(BizCodeEnum.TO_MANY_REQUEST.getCode(),
                BizCodeEnum.TO_MANY_REQUEST.getMsg());
    }
}
