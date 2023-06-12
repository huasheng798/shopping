package com.atguigu.gulimall.product.service.impl;


import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.ProductConstant;
import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.common.to.SpuBoundTo;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.entity.*;
import com.atguigu.gulimall.product.feign.CouponFeignService;
import com.atguigu.gulimall.product.feign.SearchFeignService;
import com.atguigu.gulimall.product.feign.WareFeignService;
import com.atguigu.gulimall.product.service.*;
import com.atguigu.gulimall.product.vo.*;


import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    SpuImagesService spuImagesService;

    @Autowired
    AttrService attrService;

    @Autowired
    ProductAttrValueService attrValueService;

    @Autowired
    SkuInfoService skuInfoService;

    @Autowired
    SkuImagesService skuImagesService;


    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;


    @Autowired
    WareFeignService wareFeignService;
    @Autowired
    SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }


    /**
     * 这里其实就是调用各种的表往里面保存数据，其中需要跨模块的保存数据，但没有很复杂的逻辑知识单纯的保存
     *
     * @param vo
     */
    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {
        //1、保存spu基本信息
        SpuInfoEntity infoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, infoEntity);
        infoEntity.setCreateTime(new Date());
        infoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(infoEntity);

        //2、保存Spu的描述图片
        List<String> decript = vo.getDecript();
        SpuInfoDescEntity descEntity = new SpuInfoDescEntity();//这个表只有两个数据商品id与商品介绍
        descEntity.setSpuId(infoEntity.getId());//商品id
        descEntity.setDecript(String.join(",", decript));//这个decript商品介绍，是个数组，我们使用逗号分隔一下
        spuInfoDescService.saveSpuInfoDesc(descEntity);


        //3、保存spu的图片集 pms_spu_images
        List<String> images = vo.getImages();
        spuImagesService.saveImages(infoEntity.getId(), images);//我们还要传过去给那个商品(用id)存储图片集
        //4、保存spu的规格参数 ----pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map(attr -> {
            ProductAttrValueEntity valueEntity = new ProductAttrValueEntity();//我们希望得到一个这样属性的实体类
            valueEntity.setAttrId(attr.getAttrId());
            AttrEntity byId = attrService.getById(attr.getAttrId());
            valueEntity.setAttrName(byId.getAttrName());//这里需要attr的name，所以在这里查一下
            valueEntity.setAttrValue(attr.getAttrValues());
            valueEntity.setQuickShow(attr.getShowDesc());
            valueEntity.setSpuId(infoEntity.getId());
            return valueEntity;
        }).collect(Collectors.toList());

        attrValueService.saveProductAttr(collect);
        //5、保存spu的积分信息;gulimall_sms->sms_spu_bounds
        Bounds bounds = vo.getBounds();//准备数据
        SpuBoundTo spuBoundTo = new SpuBoundTo();//常用模块中的一个文件，只要见to就应该知道是模块之间的实体类，就比如我们vo就是接收，或者传给前端的一个实体类
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(infoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundTo);//调用接口中其他模块的方法
        if (r.getCode() != 0) {
            log.error("远程保存spu积分信息失败");
        }
        //5、保存当前spu对应的所有sku信息
        List<Skus> skus = vo.getSkus();
        if (skus != null && skus.size() > 0) {
            skus.forEach(item -> {
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item, skuInfoEntity);
                skuInfoEntity.setBrandId(infoEntity.getBrandId());
                skuInfoEntity.setCatalogId(infoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(infoEntity.getId());

                //准备默认图片
                //收集信息
                String defaultImg = "";
                for (Images image : item.getImages()) {
                    defaultImg = image.getImgUrl();
                }
                //然后直接可以把默认的图片赛进去
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                //5.1)、sku的基本信息;pms_sku_info
                skuInfoService.saveSkuInfo(skuInfoEntity);
                //上面只要保存成功了以后我们就可以取到它的id
                Long skuId = skuInfoEntity.getSkuId();

                List<SkuImagesEntity> imagesEntities = item.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    //上面已经准备好数据，这面直接拿来用就行了
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    return skuImagesEntity;
                }).filter((entity -> {
                            //返回true就是需要，false就是剔除
                            return !StringUtils.isEmpty(entity.getImgUrl());
                        })
                ).collect(Collectors.toList());
                //5.2)、sku的图片信息;pms_sku_images

                skuImagesService.saveBatch(imagesEntities);


                List<Attr> attr = item.getAttr();//先取出要用的每个参数
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attr.stream().map(a -> {
                    SkuSaleAttrValueEntity attrValueEntity = new SkuSaleAttrValueEntity();//把要用的实体类new出来
                    BeanUtils.copyProperties(a, attrValueEntity);
                    attrValueEntity.setSkuId(skuId);
                    return attrValueEntity;
                }).collect(Collectors.toList());
                //5.3)、sku的销售属性信息;pms_sku_sale_attr_value
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);
                //5.4)、sku的优惠、满减等信息;gulimall_sms->sms_sku_ladder\sms_sku_full_reduction\sms_member_price
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1) {
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if (r1.getCode() != 0) {
                        log.error("远程调用保存sku优惠信息出错");
                    }
                }

            });
        }


    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity infoEntity) {
        this.baseMapper.insert(infoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((wq) -> {
                wq.eq("id", key).or().like("spu_name", key);
            });
        }
        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)) {
            wrapper.eq("publish_status", status);
        }
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            wrapper.eq("brand_id", brandId);
        }
        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            wrapper.eq("catalog_id", catelogId);
        }
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }


    @Override
    public void up(Long spuId) {
        //先创建好用于存储数据
        //   List<SkuEsModel> uoProducts = new ArrayList<>();
//        List<SkuInfoEntity> skus1 = skuInfoService.getSkusBySpuId(spuId);
//        List<Long> skuIdList = skus1.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());


        //TODO 查询当前sku的所有可以被用来检索的规格属性
        List<ProductAttrValueEntity> baseAttrs = attrValueService.baseAttrlistforspu(spuId);
        List<Long> attrIds = baseAttrs.stream().map(attr -> {
            return attr.getAttrId();
        }).collect(Collectors.toList());

        List<Long> searchAttrIds = attrService.selectSearchAttrIds(attrIds);
//        Set<Long> idSet = new HashSet<>(searchAttrIds);

        Set<Long> idSet = searchAttrIds.stream().collect(Collectors.toSet());
        List<SkuEsModel.Attrs> attrsList = baseAttrs.stream().filter(item -> {
            // contains方法依据Object的equals方法来判断是否包含某一属性
            //这个方法包含类型也必须一样，刚刚我们mapper返回的是Integer类型然后这面是long类型，一直匹配不上
            return idSet.contains(item.getAttrId());
        }).map(item -> {
            SkuEsModel.Attrs attrs = new SkuEsModel.Attrs();
            attrs.setAttrId(item.getAttrId());
            attrs.setAttrName(item.getAttrName());
            attrs.setAttrValue(item.getAttrValue());
            //BeanUtils.copyProperties(item, attrs); 粘不过来有毛病
            return attrs;
        }).collect(Collectors.toList());
        //然后组装需要的数据
        SkuEsModel skuEsModel = new SkuEsModel();
        //1.先查出当前spuid的所有sku信息，品牌名字(查出信息就好下面的操作了)
        List<SkuInfoEntity> skus = skuInfoService.getSkusBySpuId(spuId);
        List<Long> skuIdList = skus.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());
        //封装每个sku的信息

        //TODO 1.发送远程调用，库存系统查询是否有库存
        Map<Long, Boolean> stockMap = null;
        try {
            R r = wareFeignService.getSkusHasStock(skuIdList);
            //封装成Map集合，key为id，value为true或false(这里是我们自己疯转的)

            stockMap = r.getData(new TypeReference<List<SkuHasStockVo>>() {
            }).stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, item -> item.getHasStock()));
        } catch (Exception e) {
            log.error("库存服务查询异常:原因{}", e);
        }
        //不做这个会爆红
        Map<Long, Boolean> finalStockMap = stockMap;

        //封装每隔sku的信息

        List<SkuEsModel> uoProducts = skus.stream().map(sku -> {
            SkuEsModel esModel = new SkuEsModel();
            //直接先进行一次数据拷贝，把当前正在遍历的数据，对应的先给赋值上
            BeanUtils.copyProperties(sku, esModel);
            //其中里面有属性名不一致的需要单独出处理
            esModel.setSkuPrice(sku.getPrice());
            esModel.setSkuImg(sku.getSkuDefaultImg());
            //下面两个需要做一个判断，就是一个是判断是否有库存,一个是热度

            //因为这里需要远程的调用，他是个集合遍历调用，每次遍历都调用会影响速度，所以我们直接查好封装好，再拿来用
            //上面我们封装好了map，这里直接使用
            //设置库存信息
            if (finalStockMap == null) {
                esModel.setHasStock(false);
            } else {
                esModel.setHasStock(finalStockMap.get(sku.getSkuId()));
            }

            esModel.setHotScore(0L);
            //TODO 查询品牌和分类的名字西信息
            BrandEntity brand = brandService.getById(esModel.getBrandId());
            esModel.setBrandName(brand.getName());
            esModel.setBrandImg(brand.getLogo());

            CategoryEntity categoryServiceById = categoryService.getById(esModel.getCatalogId());
            esModel.setCatalogName(categoryServiceById.getName());

            //设置检索属性
            esModel.setAttrs(attrsList);
            return esModel;
        }).collect(Collectors.toList());

        //TODO 将数据发给es进行保存 gulimall-search
        R r = searchFeignService.productStatusUp(uoProducts);

        if (r.getCode() == 0) {
            //远程调用成功
            // TODO 修改当前spu的上架状态
            baseMapper.updateSpuStatus(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
        } else {
            //远程调用失败
            //TODO 重复调用 ？ 接口幕等性；重试机制?
            //Feign调用流程
            /**
             * 1.构造请求数据，将对象转为json
             * 2.发送请求进行执行(执行成功会解码响应数据);
             *    executeAndDecode(template);
             * 3.执行请求会有重试机制
             *    while(true){
             *    try{
             *    executeAndDecode(template);
             *        这里会第一次调用然后出异常了就会进入catch，就是重试机制
             *    }chtch(){
             *        retryer.continueOrPropagate(e);
             *        throw
             *    }
             */
        }
    }

    @Override
    public SpuInfoEntity getSpuInfoBySkuId(Long skuId) {
        SkuInfoEntity byId = skuInfoService.getById(skuId);
        Long spuId = byId.getSpuId();
        SpuInfoEntity spuInfo = getById(spuId);
        return spuInfo;
    }
}