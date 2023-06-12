package com.atguigu.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.service.AttrAttrgroupRelationService;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.AttrGroupRelationVo;
import com.atguigu.gulimall.product.vo.AttrGroupWithAttrsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.service.AttrGroupService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;


/**
 * 属性分组
 *
 * @author luosheng
 * @email luosheng@gmail.com
 * @date 2023-05-02 20:32:05
 */
@RestController
@RequestMapping("product/attrgroup")
@Slf4j
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    AttrService attrService;

    @Autowired
    AttrAttrgroupRelationService relationService;


    /**
     * 获取分类下所有分组&关联属性
     * /product/attrgroup/{catelogId}/withattr
     *
     * @param catelogId
     * @return
     */
    @RequestMapping("/{catelogId}/withattr")
    public R getAttrGroupWithAttrs(@PathVariable("catelogId") Long catelogId) {

        //1.查出当前分类下的所有属性分组
        List<AttrGroupWithAttrsVo> vos = attrGroupService.getAttrGroupWithAttr(catelogId);
        //2.查出每个属性分组的所有属性


        return R.ok().put("data", vos);
    }

    /**
     * /product/attrgroup/attr/relation
     * 新增关联关系(说白了就是修改一下中间表)
     *
     * @param vos
     * @return
     */
    @PostMapping("/attr/relation")
    public R addRelation(@RequestBody List<AttrGroupRelationVo> vos) {
        relationService.saveBatch(vos);
        return R.ok();
    }

    /**
     * 描述:获取属性分组里面还没有关联的本分类 里面的其他基本属性，方便添加新的管理
     * ///product/attrgroup/{attrgroupId}/noattr/relation
     *
     * @param params
     * @param attrgroupId
     * @return
     */
    @GetMapping("/{attrgroupId}/noattr/relation")
    public R attrRelation(@RequestParam Map<String, Object> params,
                          @PathVariable("attrgroupId") Long attrgroupId) {
        PageUtils page = attrService.getNoRelationAttr(params, attrgroupId);

        return R.ok().put("page", page);
    }


    /**
     * 删除属性以及属性分组
     * [{"attrId":1,"attrGroupId":2}]
     *
     * @param vos
     * @return
     */
    ///product/attrgroup/attr/relation/delete
    @PostMapping("/attr/relation/delete")
    public R relationDelete(@RequestBody AttrGroupRelationVo[] vos) {
        log.info("删除属性以及属性分组数据:{}", vos);
        attrService.deleteRelation(vos);
        return R.ok();
    }


    /**
     * 获取属性id分组 (属性分组中的关联关系)
     *
     * @param attrgroupId
     * @return
     */
    ///product/attrgroup/{attrgroupId}/attr/relation
    @GetMapping("/{attrgroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrgroupId") Long attrgroupId) {
        List<AttrEntity> attrEntities = attrService.getRelationAttr(attrgroupId);

        return R.ok().put("data", attrEntities);
    }

    /**
     * 列表
     */
    @RequestMapping("/list/{catelogId}")
    // @RequiresPermissions("product:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params,
                  @PathVariable("catelogId") Long catelogId) {

        // PageUtils page = attrGroupService.queryPage(params);
        log.info("当前list的params参数: {},catelogId: {}", params, catelogId);
        PageUtils page = attrGroupService.queryPage(params, catelogId);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    // @RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId) {
        AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);

        Long[] path = categoryService.findCatelogPath(attrGroup.getCatelogId());
        //求出完整的路径
        attrGroup.setCatelogPath(path);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup) {

        attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds) {
        attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
