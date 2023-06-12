package com.atguigu.gulimall.product.app;

import java.util.Arrays;
import java.util.List;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.common.utils.R;


/**
 * 商品三级分类
 *
 * @author luosheng
 * @email luosheng@gmail.com
 * @date 2023-05-02 20:32:05
 */
@RestController
@RequestMapping("/product/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 查询处所有分类以及子分类，以属性结构组装起来
     */
    @RequestMapping("/list/tree")
    public R list() {
        List<CategoryEntity> entities = categoryService.listWithTree();
        return R.ok().put("data", entities);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{catId}")
    // @RequiresPermissions("product:category:info")
    public R info(@PathVariable("catId") Long catId) {
        CategoryEntity category = categoryService.getById(catId);

        return R.ok().put("category", category);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("product:category:save")
    public R save( @RequestBody CategoryEntity category) {
        categoryService.save(category);

        return R.ok();
    }

    //修改拉动重新排序
    @RequestMapping("/update/sort")
    // @RequiresPermissions("product:category:update")
    public R updateSort(@RequestBody CategoryEntity[] category) {
        System.out.println(category);
        categoryService.updateBatchById(Arrays.asList(category));

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("product:category:update")
    public R update(@RequestBody CategoryEntity category) {
        categoryService.updateDetail(category);

        return R.ok();
    }

    /**
     * 删除
     *
     * @RequestBody:获取请求体,必须发送POST请求 SpringMVC自动将请求体的数据(json)
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("product:category:delete")
    public R delete(@RequestBody Long[] catIds) {
        //   categoryService.removeByIds(Arrays.asList(catIds));
        categoryService.removeMenuByIds(Arrays.asList(catIds));
        return R.ok();
    }

}
