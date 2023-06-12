package com.atguigu.gulimall.product.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @Author:luosheng
 * @Date:2023-05-24 20:45
 * @Description:
 */
@Data
@ToString
public class SkuItemSaleAttrVo {
    private Long attrId;//属性id
    private String attrName;//属性名
    private List<AttrValueWithSkuIdVo> attrValues;
}
