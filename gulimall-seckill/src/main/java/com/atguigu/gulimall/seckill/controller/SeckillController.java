package com.atguigu.gulimall.seckill.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.seckill.service.SeckillService;
import com.atguigu.gulimall.seckill.to.SecKillSkuRedisTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author:luosheng
 * @Date:2023-06-08 10:10
 * @Description:
 */
@Controller
public class SeckillController {

    @Autowired
    SeckillService seckillService;

    /**
     * 返回当前时间可以参与的秒杀商品信息
     *
     * @return
     */
    @ResponseBody
    @GetMapping("/currentSeckillSkus")
    public R getCurrentSeckillSkus() {
        List<SecKillSkuRedisTo> vos = seckillService.getCurrentSeckillSkus();
        return R.ok().setData(vos);
    }

    @ResponseBody
    @GetMapping("/sku/seckill/{skuId}")
    public R getSkuSeckillInfo(@PathVariable("skuId") Long skuId) {

        SecKillSkuRedisTo to = seckillService.getSkuSeckillInfo(skuId);
        return R.ok().setData(to);
    }

    @GetMapping("/kill")
//    killId=2_2&key=9702bc9700c246e3a2213dd0b0da5507&num=1
    //要秒杀的id，和秒杀的key做随机数的那个比较判断，和秒杀的数量
    public String secKill(@RequestParam("killId") String killId,
                          @RequestParam("key") String key,
                          @RequestParam("num") Integer num,
                          Model model) throws InterruptedException {
        //1.判断是否登录（有拦截器了）

        String orderSn = seckillService.kill(killId, key, num);//秒杀成功返回一个订单号
        //然后能取到说明成功，取不到说明失败
        model.addAttribute("orderSn", orderSn);
        return "success";
    }
}
