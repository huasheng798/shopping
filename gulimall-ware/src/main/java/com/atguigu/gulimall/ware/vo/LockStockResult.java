package com.atguigu.gulimall.ware.vo;

import lombok.Data;

/**
 * @Author:luosheng
 * @Date:2023-06-05 8:55
 * @Description:
 */
@Data
public class LockStockResult {
    private Long skuId;//那个商品
    private Integer num;//锁了几件
    private Boolean locked;//是否锁定成功
}
