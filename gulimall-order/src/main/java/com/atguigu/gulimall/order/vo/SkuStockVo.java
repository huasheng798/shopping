package com.atguigu.gulimall.order.vo;

import lombok.Data;

/**
 * @Author:luosheng
 * @Date:2023-06-04 9:48
 * @Description:
 */
@Data
public class SkuStockVo {
    private  Long skuId;
    private Boolean hasStock;
}
