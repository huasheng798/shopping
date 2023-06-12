package com.atguigu.gulimall.product.web;


import com.atguigu.gulimall.product.service.SkuInfoService;
import com.atguigu.gulimall.product.vo.SkuItemVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author:luosheng
 * @Date:2023-05-24 17:08
 * @Description:
 */
@Controller
@Slf4j
public class itemController {

    @Autowired
    SkuInfoService skuInfoService;

    @RequestMapping("/{skuId}.html")
    public String item(@PathVariable("skuId") Long skuId, Model model) {
//        log.info("当前要查询的商品id:{}", skuId);   这块不知道为什么不会输出内容
        System.out.println("当前要查询的商品id:" + skuId);
        SkuItemVo skuItemVo = skuInfoService.item(skuId);
        model.addAttribute("item", skuItemVo);
        return "item";
    }

}
