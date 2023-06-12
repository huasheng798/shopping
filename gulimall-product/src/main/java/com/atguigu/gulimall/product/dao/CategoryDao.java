package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author luosheng
 * @email luosheng@gmail.com
 * @date 2023-05-02 20:32:05
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
