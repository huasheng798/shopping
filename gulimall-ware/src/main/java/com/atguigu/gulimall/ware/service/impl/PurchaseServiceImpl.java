package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.constant.WareConstant;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.atguigu.gulimall.ware.vo.MergeVo;
import com.atguigu.gulimall.ware.entity.PurchaseDetailEntity;
import com.atguigu.gulimall.ware.service.PurchaseDetailService;
import com.atguigu.gulimall.ware.vo.PurchaseDoneVo;
import com.atguigu.gulimall.ware.vo.PurchaseItemDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.ware.dao.PurchaseDao;
import com.atguigu.gulimall.ware.entity.PurchaseEntity;
import com.atguigu.gulimall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    PurchaseDetailService detailService;

    @Autowired
    WareSkuService wareSkuService;

    /**
     * 条件
     * status: 0,//状态
     * wareId: 1,//仓库id
     * key: '华为',//检索关键字
     *
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> queryWrapper = new QueryWrapper<>();


        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceive(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> queryWrapper = new QueryWrapper<>();
        //状态[0新建，1已分配，2正在采购，3已完成，4采购失败]
        queryWrapper.eq("status", 0).or().eq("status", 1);
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void mergePurchase(MergeVo mergeVo) {
        //我们有两种采购单过来的方式
        //如果没有采购单的采购单id 也就是purchaseId 那说明是新建的
        Long purchaseId = mergeVo.getPurchaseId();
        if (mergeVo.getPurchaseId() == null) {
            //直接新建采购单
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            purchaseEntity.setUpdateTime(new Date());
            purchaseEntity.setCreateTime(new Date());
            this.save(purchaseEntity);
            //建完以后我们再把它的id给赋值mergeVo.getPurchaseId()本来不是null现在就有id了
            purchaseId = purchaseEntity.getId();
        }
        //TODO 这里要判断一下，采购单的状态才能合并
        //不为空就可以直接有id就是有采购单id，我们直接合并进去
        List<Long> items = mergeVo.getItems();
        //合并其实就是一步修改，它修改的是我们采购的采购purchase_id把purchase这个表的id给他这个字段。,然后再修改它的需求状态

        //这个大致的意思就是说，Lambda 表达式中要用到的，但又未在 Lambda 表达式中声明的变量，必须声明为 final
        // 或者是 effectively final，否则就会出现编译错误。
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> collect = items.stream().map(i -> {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();

            detailEntity.setId(i);
            detailEntity.setPurchaseId(finalPurchaseId);
            detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());//这个给他一个已分配的状态码，也就是1
            return detailEntity;
        }).collect(Collectors.toList());

        detailService.updateBatchById(collect);
        //在采购单合并的时候或者新建，我们直接让他的时间更新一下
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }

    @Transactional
    @Override
    public void received(List<Long> ids) {//这里的ids是采购单的id
        //领取采购单的几个步骤
        //1.确认当前采购单是新建或者已分配状态
        List<PurchaseEntity> collect = ids.stream().map(id -> {
            //先根据每个id查询
            PurchaseEntity byId = this.getById(id);
            return byId;
        }).filter(purchaseEntity -> {
            //如果它的状态是未分配和新建，就可以领取‘
            if (purchaseEntity.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode() ||
                    purchaseEntity.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()) {
                return true;
            }
            return false;
        }).map(item -> {
            //在这里改造一下，把当前的状态改成已经领取
            item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());
            item.setUpdateTime(new Date());//修改时间也填上
            return item;
        }).collect(Collectors.toList());
        //2.改变采购单的状态
        this.updateBatchById(collect);
        //3.改变采购项的状态
        collect.forEach((item) -> {
            //TODO 这里优点问题，如果采购单为空的话 领取不到它的单子
            List<PurchaseDetailEntity> entities = detailService.listDetailByPurchaseId(item.getId());
            List<PurchaseDetailEntity> collect1 = entities.stream().map(entitie -> {
                PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
                detailEntity.setId(entitie.getId());
                detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());//改未正在采购
                return detailEntity;
            }).collect(Collectors.toList());
            detailService.updateBatchById(collect1);
        });
    }

    @Override
    public void done(PurchaseDoneVo doneVo) {


        //1.改变采购项的状态
        Boolean flag = true;//存储状态
        List<PurchaseItemDoneVo> items = doneVo.getItems();
        //new给List用于存储所有遍历的实体类
        ArrayList<PurchaseDetailEntity> updates = new ArrayList<>();
        for (PurchaseItemDoneVo item : items) {
            //把实体类new出来一会用于存储数据且更新数据
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            //如果它采购失败了那么flag未false
            if (item.getStatus() == WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode()) {
                flag = false;
                detailEntity.setStatus(item.getStatus());
            }else{
                //否则就采购成功设置状态为成功
                detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISH.getCode());
                //3.将成功采购的进行入库
                //我们这里需要一个采购项的id，和数量，所以我们可以根据ItemId查出采购项的实体信息
                PurchaseDetailEntity entity = detailService.getById(item.getItemId());
                wareSkuService.addStock(entity.getSkuId(),entity.getWareId(),entity.getSkuNum());
            }
            detailEntity.setId(item.getItemId());
            updates.add(detailEntity);
        }
        //更新

        detailService.updateBatchById(updates);

        //2.改变采购单状态
        Long id = doneVo.getId();
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setUpdateTime(new Date());
        //这个状态要看采购项的状态(我们不是定义了一个flag如果为true说明成功，false说明失败)
        purchaseEntity.setStatus(flag?WareConstant.PurchaseStatusEnum.FINISH.getCode():
                                 WareConstant.PurchaseStatusEnum.HASERROR.getCode());
        this.updateById(purchaseEntity);





    }
}