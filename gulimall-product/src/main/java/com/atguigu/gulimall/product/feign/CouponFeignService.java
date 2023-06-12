package com.atguigu.gulimall.product.feign;

import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.common.to.SpuBoundTo;
import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Author:luosheng
 * @Date:2023-05-13 10:30
 * @Description: 用于调用优惠劵服务所有内容
 */
@FeignClient("gulimall-coupon")//调用远程服务的名字
public interface CouponFeignService {
    /**
     * 如果有一个方法里调用了 CouponFeignService.saveSpuBounds(spuBoundTo) 的方法并传入了一个对象
     *       1)、@RequestBody 将这个对象转为json.
     *       2)、他会找到gulimall-coupon这个的服务名字，给他的/coupon/spubounds/save路径发送请求,
     *       他会将上一步转的json方在请求体位置，发送请求;
     *       3)、对方服务收到请求。请求体里有json数据。
     *          (@RequestBody SpuBoundsEntity spuBounds); 将请求体的json转为SpuBoundsEntity;
     *          //反正这块意思就是只要你转换json对应 就ok没有说类型必须一模一样
     * 只要json数据模型是兼容的。双方服务无需使用同一个to
     * @param spuBoundTo
     * @return
     */
    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundTo spuBoundTo);


    @PostMapping("/coupon/skufullreduction/saveinfo")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);
}
