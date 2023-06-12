package com.atguigu.gulimall.product;

import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.dao.SkuSaleAttrValueDao;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.gulimall.product.service.SkuSaleAttrValueService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;


import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;


import java.lang.reflect.Array;
import java.util.List;
import java.util.UUID;

/**
 * 1.引入oss-starter
 * 2.配置key，endpoint相关信息即可
 * 3.适用OSSClient然后自动状态
 */
@SpringBootTest
class GulimallProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Autowired
    StringRedisTemplate redisTemplate;


    @Autowired
    RedissonClient redissonClient;
    @Autowired
    AttrGroupDao attrGroupDao;

    @Autowired
    SkuSaleAttrValueDao skuSaleAttrValueDao;

    @Test
    public void test() {
        System.out.println(skuSaleAttrValueDao.getSaleAttrsBySpuId(6L));
    }

    @Test
    public void redissontest() {
        System.out.println(redisTemplate);
    }

    @Test
    public void teststringRedisTemplate() {
        //hello world
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        //保存
        ops.set("hello", "world_" + UUID.randomUUID().toString());
        //查询
        String v = ops.get("hello");
        System.out.println(v);
    }

    @Test
    void Test() {
        int a[] = {5, 22, 55, 33, 66, 88, 77};
        for (int i = 0; i < a.length - 1; i++) {
            for (int j = 0; j < a.length  - 1- i; j++) {
                if (a[j] > a[j+1]) {
                    //替换位置
                   int c=a[j+1];
                   a[j+1]=a[j];
                   a[j]=c;
                }
            }
        }
        for (int b : a
        ) {
            System.out.println(b);
        }

    }

    @Test
    void contextLoads() {

        BrandEntity brandEntity = new BrandEntity();
      /*  brandEntity.setDescript("测试是否自增");
        brandEntity.setName("华为");
        brandService.save(brandEntity);
        System.out.println("保存成功");*/


  /*      brandEntity.setBrandId(1L);
        brandEntity.setDescript("测试是否可修改");
        brandService.updateById(brandEntity);
        System.out.println("修改chenggong");*/
        List<BrandEntity> brand_id = brandService.list(
                new QueryWrapper<BrandEntity>().ge("brand_id", 1L));
        brand_id.forEach(System.out::println);
    }


}
