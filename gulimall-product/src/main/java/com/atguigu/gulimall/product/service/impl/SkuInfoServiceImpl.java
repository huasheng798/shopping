package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import com.atguigu.gulimall.product.feign.SeckillFeignService;
import com.atguigu.gulimall.product.service.*;
import com.atguigu.gulimall.product.vo.SeckillInfoVo;
import com.atguigu.gulimall.product.vo.SkuItemSaleAttrVo;
import com.atguigu.gulimall.product.vo.SkuItemVo;
import com.atguigu.gulimall.product.vo.SpuItemAttrGroupVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.SkuInfoDao;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import org.springframework.util.StringUtils;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    SkuImagesService imagesService;

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    AttrGroupService attrGroupService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    SeckillFeignService seckillFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        /**
         * key:
         * catelogId: 0
         * brandId: 0
         * min: 0
         * max: 0
         */
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();
        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            queryWrapper.eq("catalog_id", catelogId);
        }
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            queryWrapper.eq("brand_id", brandId);
        }

        String min = (String) params.get("min");
        if (!StringUtils.isEmpty(min)) {
            queryWrapper.ge("price", min);
        }
        String max = (String) params.get("max");
        if (!StringUtils.isEmpty(max)) {
            //为了别在这里因为转换而报错，比如传来了一个字母（虽然前端也会有判断）,这里就抛下异常
            try {
                //这边的值不能是string
                BigDecimal bigDecimal = new BigDecimal(max);
                //bigDecimal>new BigDecimal("0") 时result=1;
                //bigDecimal=new BigDecimal("0") 时result=0;
                //这个就是一个判断 也就是说如果max（bigDecimal）大于0 则返回1
                if (bigDecimal.compareTo(new BigDecimal("0")) == 1) {
                    queryWrapper.le("price", max);
                }
            } catch (Exception e) {

            }

        }

        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        //先根据spuid查询

        List<SkuInfoEntity> spu_id = this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));

        return spu_id;
    }


    @Override
    public SkuItemVo item(Long skuId) {
        SkuItemVo skuItemVo = new SkuItemVo();
        //1.sku基本信息获取  pms_sku_info
        SkuInfoEntity skuInfo = this.getById(skuId);
        skuItemVo.setInfo(skuInfo);
        Long catalogId = skuInfo.getCatalogId();//拿出分类id一会根据这个可以查询获取spu的规格参数信息
        //4.获取spu的介绍 这一块其实就是一个图片里面有很多描述 也就查询一个表,pms_spu_info_desc(还有其他地方也可以用xxx)
        Long spuId = skuInfo.getSpuId();
        //2.获取sku的图片的信息  pms_sku_images
        List<SkuImagesEntity> images = imagesService.getImagesBySkuId(skuId);
        skuItemVo.setImages(images);
        //3.获取spu的销售属性组合。
        List<SkuItemSaleAttrVo> saleAttrVos = skuSaleAttrValueService.getSaleAttrsBySpuId(spuId);
        skuItemVo.setSaleAttr(saleAttrVos);

        SpuInfoDescEntity spuInfoDesc = spuInfoDescService.getById(spuId);
        skuItemVo.setDesc(spuInfoDesc);
        //5.获取spu的规格参数信息。
        List<SpuItemAttrGroupVo> attrGroupVos = attrGroupService.getAttrGroupWithBySpuId(spuId, catalogId);
        skuItemVo.setGroupAttrs(attrGroupVos);

        //3.查询当前sku是否参与秒杀优惠
        R seckillInfo = seckillFeignService.getSkuSeckillInfo(skuId);
        if (seckillInfo.getCode() == 0) {
            SeckillInfoVo seckillInfoVo = seckillInfo.getData(new TypeReference<SeckillInfoVo>() {
            });
            skuItemVo.setSeckillInfo(seckillInfoVo);
        }
        return skuItemVo;
    }


}

