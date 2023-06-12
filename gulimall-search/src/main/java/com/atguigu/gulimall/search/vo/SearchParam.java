package com.atguigu.gulimall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author:luosheng
 * @Date:2023-05-20 15:41
 * @Description: 主要用于
 * 封装页面所有可能传递过来的查询条件
 * <p>
 * catalog3Id=225&keyword=小米&sort=saleCount_sac&hasStock=0/1
 */
@Data
public class SearchParam {
    private String keyword;//页面传递过来的全文匹配关键字
    private Long catalog3Id;//三级分类id
    /**
     * sort=saleCount_asc/desc
     * sort=skuPrice_asc/desc
     * sort=hotScore_asc/desc
     */
    private String sort;//排序条件

    /**
     * 好多的过滤条件
     * hasStock(是否有货0/1),skuPrice区间，brandId，catalog3Id、attrs
     * hasStock=0/1
     * skuPrice=1_500/_500/500_
     */
    private Integer hasStock;//是否只显示有货  v 0(无库存) 1(有库存)
    private String skuPrice;//价格区间查询
    private List<Long> brandId;//按照品牌进行查询,可以多选
    private List<String> attrs;//按照属性进行筛选
    private Integer pageNum = 1;//页码

    private String _queryString;//所有原生的查询体条件
}
