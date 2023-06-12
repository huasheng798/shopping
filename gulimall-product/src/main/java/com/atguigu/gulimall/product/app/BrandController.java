package com.atguigu.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.atguigu.common.valid.AddGroup;
import com.atguigu.common.valid.UpdateGroup;
import com.atguigu.common.valid.UpdateStatusGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;


/**
 * 品牌
 *
 * @author luosheng
 * @email luosheng@gmail.com
 * @date 2023-05-02 20:32:05
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("product:brand:list")
    public R list(@RequestParam Map<String, Object> params) {


        PageUtils page = brandService.queryPage(params);


        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    // @RequiresPermissions("product:brand:info")
    public R info(@PathVariable("brandId") Long brandId) {
        BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    @GetMapping("/infos")
    // @RequiresPermissions("product:brand:info")
    public R infos(@RequestParam("brandids") List<Long> brandids) {
        List<BrandEntity> brand = brandService.getBrandByIds(brandids);
        return R.ok().put("brand", brand);
    }


    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("product:brand:save")
    //@Validated 这个注解是当前字段/实体类中要进行后端校验,后面的参数是，当前要走添加逻辑分组的条件
    //BindingResult它的值就可以获取到校验的结果
    public R save(@Validated({AddGroup.class}) @RequestBody BrandEntity brand /*, BindingResult result*/) {
        //这块本是处理异常，但我们完全可以写一个异常的类来进行异常的统一处理
/*        if (result.hasErrors()) {
            Map<String,String> map=new HashMap<>();
            result.getFieldErrors().forEach((item) -> {
                //FieldError 获取到错误提示
                String message = item.getDefaultMessage();
                //获取错误的属性的名字
                String field = item.getField();
                map.put(field,message);

            });
            return R.error(400, "提交的数据不合法").put("data",map);
        } else {
            brandService.save(brand);
        }*/
        brandService.save(brand);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("product:brand:update")
    public R update(@Validated(UpdateGroup.class) @RequestBody BrandEntity brand) {
        brandService.updateDetail(brand);

        return R.ok();
    }

    /**
     * 修改状态
     */
    @RequestMapping("/update/status")
    // @RequiresPermissions("product:brand:update")
    public R updateStatus(@Validated(UpdateStatusGroup.class) @RequestBody BrandEntity brand) {
        brandService.updateById(brand);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("product:brand:delete")
    public R delete(@RequestBody Long[] brandIds) {
        brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
