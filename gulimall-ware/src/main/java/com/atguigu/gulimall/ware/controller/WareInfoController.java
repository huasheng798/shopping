package com.atguigu.gulimall.ware.controller;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.atguigu.gulimall.ware.vo.FareVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.ware.entity.WareInfoEntity;
import com.atguigu.gulimall.ware.service.WareInfoService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;


/**
 * 仓库信息
 *
 * @author luosheng
 * @email luosheng@gmail.com
 * @date 2023-05-02 21:15:27
 */
@RestController
@RequestMapping("ware/wareinfo")
public class WareInfoController {
    @Autowired
    private WareInfoService wareInfoService;

    /**
     * 获取运费
     */
    @GetMapping("/fare")
    public R getFare(@RequestParam("addrId") Long addrId) {//这里要根据收获地址计算
        FareVo fare = wareInfoService.getFare(addrId);
        return R.ok().setData(fare);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("ware:wareinfo:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = wareInfoService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("ware:wareinfo:info")
    public R info(@PathVariable("id") Long id) {
        WareInfoEntity wareInfo = wareInfoService.getById(id);

        return R.ok().put("wareInfo", wareInfo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("ware:wareinfo:save")
    public R save(@RequestBody WareInfoEntity wareInfo) {
        wareInfoService.save(wareInfo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("ware:wareinfo:update")
    public R update(@RequestBody WareInfoEntity wareInfo) {
        wareInfoService.updateById(wareInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("ware:wareinfo:delete")
    public R delete(@RequestBody Long[] ids) {
        wareInfoService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
