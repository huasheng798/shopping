package com.atguigu.gulimall.ware.service.impl;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.ware.dao.PurchaseDetailDao;
import com.atguigu.gulimall.ware.entity.PurchaseDetailEntity;
import com.atguigu.gulimall.ware.service.PurchaseDetailService;
import org.springframework.util.StringUtils;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<PurchaseDetailEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(w->{
                w.eq("purchase_id", key).or().like("sku_id", key);
            });
        }
        //状态[0新建，1已分配，2正在采购，3已完成，4采购失败]
        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)) {

            queryWrapper.eq("status", status);
        }

        String wareId = (String) params.get("wareId");

        if (!StringUtils.isEmpty(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }
        IPage<PurchaseDetailEntity> page = this.page(
                new Query<PurchaseDetailEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<PurchaseDetailEntity> listDetailByPurchaseId(Long id) {
        //我们要根据它的purchaseEntity的id也就是传来的id
        // 修改PurchaseDetailEntity这个的状态
        //这里只需要查找所有信息返回回去，其他操作调用者会做
        List<PurchaseDetailEntity> purchase_id =
                this.list(new QueryWrapper<PurchaseDetailEntity>().eq("purchase_id", id));

        return purchase_id;
    }

}