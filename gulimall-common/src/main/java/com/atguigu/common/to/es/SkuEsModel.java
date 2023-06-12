package com.atguigu.common.to.es;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author:luosheng
 * @Date:2023-05-16 9:26
 * @Description:
 */
@Data
public class SkuEsModel {

    private Long skuId;
    private Long spuId;
    private String skuTitle;
    private BigDecimal skuPrice;
    private String skuImg;
    private Long saleCount;
    private Boolean hasStock;
    private Long hotScore;
    private Long brandId;
    private Long catalogId;
    private String brandName;
    private String brandImg;
    private String catalogName;
    private List<Attrs> attrs;
    @Data
    //为了保证其他第三方工具可以序列化或反序列化，给他设上public可访问
    public static class Attrs {
        private Long attrId;
        private String attrName;
        private String attrValue;
    }
}
