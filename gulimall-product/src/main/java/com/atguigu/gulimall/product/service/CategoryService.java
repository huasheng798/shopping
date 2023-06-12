package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author luosheng
 * @email luosheng@gmail.com
 * @date 2023-05-02 20:32:05
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree();

    void removeMenuByIds(List<Long> asList);

    /**
     * 找到caelogId的完整路径
     * [父/子/孙]
     *
     * @param catelogId
     * @return
     */
    Long[] findCatelogPath(Long catelogId);


  void updateDetail(CategoryEntity attrGroup);

    List<CategoryEntity> getLevel1Categorys();

    Map<String, List<Catelog2Vo>> getCatalogJson();
}

