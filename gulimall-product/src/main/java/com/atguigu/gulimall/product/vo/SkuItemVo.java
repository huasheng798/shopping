package com.atguigu.gulimall.product.vo;

import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

/**
 * @Author:luosheng
 * @Date:2023-05-24 17:41
 * @Description:
 */
@Data
public class SkuItemVo {

    //1.sku基本信息获取  pms_sku_info
    private SkuInfoEntity info;

    private boolean hasStock = true; //是否有货
    //2.获取sku的图片的信息  pms_sku_images
    private List<SkuImagesEntity> images;
    //3.获取spu的销售属性组合。
    private List<SkuItemSaleAttrVo> saleAttr;
    //4.获取spu的介绍 这一块其实就是一个图片里面有很多描述
    private SpuInfoDescEntity desc;

    //5.获取spu的规格参数信息。
    private List<SpuItemAttrGroupVo> groupAttrs;

    //当前商品的秒杀优惠信息
    SeckillInfoVo seckillInfo;

}
