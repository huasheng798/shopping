package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author:luosheng
 * @Date:2023-06-03 16:54
 * @Description:
 */
@Data
public class OrderItemVo {

    private Long skuId;
    private Boolean check = true;
    private String title;
    private String image;
    private List<String> skuAttr;
    private BigDecimal price;
    private Integer count;
    private BigDecimal totalPrice;
    private boolean hasStock;//是否有货(暂时无用，我们检查是否有库存单写了个方法)
    /** 商品重量 **/
    private BigDecimal weight = new BigDecimal("0.085");



}
