package com.atguigu.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.atguigu.gulimall.product.entity.ProductAttrValueEntity;
import com.atguigu.gulimall.product.service.ProductAttrValueService;
import com.atguigu.gulimall.product.vo.AttrRespVo;
import com.atguigu.gulimall.product.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;


/**
 * 商品属性
 *
 * @author luosheng
 * @email luosheng@gmail.com
 * @date 2023-05-02 20:32:05
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;


    ///product/attr/update/{spuId}
    //    修改商品规格
//    [{
//	"attrId": 7,
//	"attrName": "入网型号",
//	"attrValue": "LIO-AL00",
//	"quickShow": 1
//}, {
//	"attrId": 14,
//	"attrName": "机身材质工艺",
//	"attrValue": "玻璃",
//	"quickShow": 0
    @PostMapping("/update/{spuId}")
    public R updatespuId(@PathVariable("spuId") Long spuId,
                         @RequestBody List<ProductAttrValueEntity> entities) {
           productAttrValueService.updateSpuAttr(spuId,entities);
        return R.ok();
    }

    @Autowired
    ProductAttrValueService productAttrValueService;

    ///product/attr/base/listforspu/{spuId}  //获取spu规格
    @GetMapping("/base/listforspu/{spuId}")
    public R baseAttrlistforspu(@PathVariable("spuId") Long spuId) {
        List<ProductAttrValueEntity> entities = productAttrValueService.baseAttrlistforspu(spuId);
        return R.ok().put("data", entities);
    }

    /**
     * 销售属性查询，但我们试试这个办法，就是一个方法两个用在底下会使用
     * @param params
     * @param catelogId
     * @return
     */
//    @GetMapping("/sale/list/{catelogId}")
//    public R saleAttrList(@RequestParam Map<String, Object> params,
//                          @PathVariable("catelogId") Long catelogId) {
//        PageUtils page=attrService.queryBaseAttrPage(params,catelogId);
//        return R.ok().put("page",page);
//    }

    /**
     * 实现展示数据 规格参数
     *
     * @param params
     * @param catelogId
     * @return // /product/attr/base/list/{catelogId}
     */

    @GetMapping("/{attrType}/list/{catelogId}")
    public R baseAttrList(@RequestParam Map<String, Object> params,
                          @PathVariable("catelogId") Long catelogId,
                          @PathVariable("attrType") String attrType) {
        PageUtils page = attrService.queryBaseAttrPage(params, catelogId, attrType);
        return R.ok().put("page", page);
    }


    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("product:attr:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     * 用于修改后的回显
     */
    @RequestMapping("/info/{attrId}")
    // @RequiresPermissions("product:attr:info")
    public R info(@PathVariable("attrId") Long attrId) {
        //   AttrEntity attr = attrService.getById(attrId);
        AttrRespVo attr = attrService.getAttrInfo(attrId);
        return R.ok().put("attr", attr);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("product:attr:save")
    public R save(@RequestBody AttrVo attr) {
        attrService.saveAttr(attr);

        return R.ok();
    }

    /**
     * 修改
     * 修改功能也需要改造，我们要修改的是多个表的数据.
     * 因为前端传来的是多个表的数据，所以这里拿AttrVo来接收
     */
    @RequestMapping("/update")
    // @RequiresPermissions("product:attr:update")
    public R update(@RequestBody AttrVo attr) {
        attrService.updateAttr(attr);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("product:attr:delete")
    public R delete(@RequestBody Long[] attrIds) {
        attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
