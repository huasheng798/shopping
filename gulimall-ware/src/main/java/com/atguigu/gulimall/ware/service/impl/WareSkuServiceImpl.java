package com.atguigu.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.mq.OrderTo;
import com.atguigu.common.mq.StockDetailTo;
import com.atguigu.common.mq.StockLockedTo;
import com.atguigu.common.utils.R;
import com.atguigu.common.exception.NoStockException;
import com.atguigu.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.atguigu.gulimall.ware.entity.WareOrderTaskEntity;
import com.atguigu.gulimall.ware.feign.OrderFeignService;
import com.atguigu.gulimall.ware.feign.ProductFeignService;
import com.atguigu.gulimall.ware.service.WareOrderTaskDetailService;
import com.atguigu.gulimall.ware.service.WareOrderTaskService;
import com.atguigu.gulimall.ware.vo.OrderItemVo;
import com.atguigu.gulimall.ware.vo.OrderVo;
import com.atguigu.gulimall.ware.vo.SkuHasStockVo;
import com.atguigu.gulimall.ware.vo.WareSkuLockVo;
import com.rabbitmq.client.Channel;
import lombok.Data;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.ware.dao.WareSkuDao;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

//"stock-event-exchange","stock.locked",
@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    WareSkuDao wareSkuDao;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    WareOrderTaskService orderTaskService;
    @Autowired
    WareOrderTaskDetailService orderTaskDetailService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    OrderFeignService orderFeignService;


    private void unLockStock(Long skuId, Long wareId, Integer num, Long taskDetailId) {
//update wms_ware_sku set stock_locked=stock_locked-1 where sku_id=1 and ware_id=2
        //库存解锁
        wareSkuDao.unlockStock(skuId, wareId, num);
        //更新库存工作单的状态
        WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity();
        entity.setId(taskDetailId);
        entity.setLockStatus(2);//变为已解锁
        orderTaskDetailService.updateById(entity);
    }

    /**
     * skuId: 1
     * wareId: 2
     *
     * @param params
     * @return
     */
    @Transactional
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            queryWrapper.eq("sku_id", skuId);
        }
        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }


        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        //如果没有skuid和wareid那么我们要执行的就是新增操作
        List<WareSkuEntity> wareSkuEntities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).
                eq("ware_id", wareId));
        if (wareSkuEntities.size() == 0) {
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStockLocked(0);
            //TODO 远程查询sku的名字，如果失败，整个事务无需回滚
            //这里有很多说法，因为它就是一个冗余字段没什么核心业务，如果因为服务器等不稳定这个远程调用失败了
            //而导致整个事务的回滚，所以这里 第一个方法就是它出异常了我们直接不管
            //todo 第二种方式出现异常不回滚，后面会说
            try {
                R info = productFeignService.info(skuId);
                Map<String, Object> skuInfo = (Map<String, Object>) info.get("skuInfo");
                if (info.getCode() == 0) {//0为成功
                    wareSkuEntity.setSkuName((String) skuInfo.get("skuName"));
                }
            } catch (Exception e) {

            }


            wareSkuDao.insert(wareSkuEntity);
        } else {
            wareSkuDao.addStock(skuId, wareId, skuNum);//采购id，仓库id，采购的数量
        }


    }

    @Override
    public List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds) {
        List<SkuHasStockVo> collect = skuIds.stream().map(skuId -> {
            SkuHasStockVo vo = new SkuHasStockVo();
            //查询当前sku的总库存量
            //SELECT SUM(stock-stock_locked) FROM wms_ware_sku WHERE sku_id=1
            Long count = baseMapper.getSkuStock(skuId);
            vo.setSkuId(skuId);
            vo.setHasStock(count == null ? false : count > 0);
            return vo;
        }).collect(Collectors.toList());
        return collect;
    }

    /**
     * 为某个订单锁定库存
     * (rollbackFor = NoStockException.class) 指定某个异常回滚
     * 默认只要是运行时异常都会回滚
     * <p>
     * 库存解锁的场景
     * 1)、下订单成功，订单过期没有支付系统自动取消，被用户手动取消。都要解锁库存
     * 2)、
     *
     * @param vo
     * @return
     */
    @Transactional(rollbackFor = NoStockException.class)
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {
        /**
         * 我们只要一进来先保存库存工作单
         * 保存库存工作单的详情。
         * 追溯。
         */
        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderSn(vo.getOrderSn());
        orderTaskService.save(taskEntity);

        //1.按照下单的收货地址，找到一个就近仓库，锁定库存。
        //1.找到每个商品在那个残酷都有库存
        List<OrderItemVo> locks = vo.getLocks();

        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock stock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            stock.setNum(item.getCount());
            //查询这个商品在哪里有库存
            List<Long> wareIds = wareSkuDao.listWareIdHasSkuStock(skuId);
            stock.setWareId(wareIds);//把有库存的id放进去
            return stock;
        }).collect(Collectors.toList());


        Boolean allLock = true;//先定义一个全局变量，默认给他true
        //2.锁定库存
        for (SkuWareHasStock hasStock : collect) {
            Boolean skuStocked = false;
            Long skuId = hasStock.getSkuId();//拿出skuid
            List<Long> wareIds = hasStock.getWareId();//拿出库存id
            if (wareIds == null || wareIds.size() == 0) {
                //说明灭有任何仓库有这个仓库的库存
                //直接抛异常
                throw new NoStockException(skuId);
            }
            //1.如果每一个商品都锁定成功，将当前商品锁定了几件的工作单记录发送给MQ
            //2.如果锁定失败.前面保存的工作单信息就回滚了。发送出去的消息，即使要解锁记录，由于去数据库查不到id，所以就不用解锁
            for (Long wareId : wareIds) {
                //成功返回1，否则返回0
                Long count = wareSkuDao.lockSkuStock(skuId, wareId, hasStock.getNum());
                if (count == 1) {
                    //只要有一个成功，直接为true  就已经锁住了
                    skuStocked = true;
                    //todo  告诉MQ库存锁定成功
                    //保存成功了以后，保存一个成功的库存详情表
                    WareOrderTaskDetailEntity orderTaskEntity = new WareOrderTaskDetailEntity(null, skuId, "",
                            hasStock.getNum(), taskEntity.getId(), wareId, 1);
                    orderTaskDetailService.save(orderTaskEntity);
                    //这里锁成功了发送消息
                    StockDetailTo stockDetailTo = new StockDetailTo();
                    BeanUtils.copyProperties(orderTaskEntity, stockDetailTo);
                    StockLockedTo lockedTo = new StockLockedTo();
                    lockedTo.setId(taskEntity.getId());
                    //这里要传送一个实体类的所有信息，也就是详情单的所有信息，防止回滚以后找不到数据
                    lockedTo.setDetailId(stockDetailTo);
                    rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", lockedTo);
                    break;
                } else {
                    //当前仓库锁失败，重试下一个仓库
                }
                if (skuStocked == false) {
                    //当前商品所有仓库都没有锁住
                    throw new NoStockException(skuId);
                }
            }
        }

        //如果能走到这里 说明肯定锁成功了

        return true;
    }

    @Override
    public void unlockStock(StockLockedTo to) {
        System.out.println("收到解锁库存的消息");
        StockDetailTo detailId = to.getDetailId();
        Long skuId = detailId.getSkuId();//这里面有很多详细的信息，比如锁了几件商品，可以用来进行回滚
        Long detailIdId = detailId.getId();
        //进行解锁
        //1.先查询数据库关于这个订单的锁定信息，
        //有：证明库存锁定成功了
        //   解锁:订单情况.
        //1.没有这个订单。必须解锁
        //2.有这个订单
        //没有:库存锁定失败了。库存回滚了。这种情况无需解锁
        WareOrderTaskDetailEntity byId = orderTaskDetailService.getById(detailIdId);
        if (byId != null) {
            //解锁
            Long id = to.getId();//库存工作单的id
            WareOrderTaskEntity taskEntity = orderTaskService.getById(id);
            String orderSn = taskEntity.getOrderSn();//根据订单号查询订单的状态
            R orderStatus = orderFeignService.getOrderStatus(orderSn);
            if (orderStatus.getCode() == 0) {
                //订单数据返回成功
                OrderVo data = orderStatus.getData(new TypeReference<OrderVo>() {
                });
                if (data == null || data.getStatus() == 4) {
                    //订单不存在也解锁
                    //订单已经被取消了、才能解锁库存
                    if (byId.getLockStatus() == 1) {
                        //当前库存工作单详情，状态为1(锁定状态) 才可以解锁
                        unLockStock(detailId.getSkuId(), detailId.getWareId(),
                                detailId.getSkuNum(), detailIdId);
                    }
                    //开启手动回复，解锁了再让队列的消息删除
                }
            } else {
                //失败了。我们不手动回复，他就不会删除
                //采用消息拒绝直接，重新放到队列里面，让别人继续消费解锁
                throw new RuntimeException("远程服务失败");
            }
        } else {
            //无需解锁
        }

    }

    /**
     * 订单传来的解锁库存
     * 主要防止订单服务卡顿，导致订单状态消息一直改不了，库存消息优先到期，查订单状态新建状态，什么都不做就走了
     * 导致卡顿的订单，永远不能解锁库存
     *
     * @param order
     */
    @Transactional
    @Override
    public void unlockStock(OrderTo order) {
        String orderSn = order.getOrderSn();
        //查一下最新库存的状态，防止重复解锁库存
        //查出它就可以根据它的id查询详情那个单(库存工作单wms_ware_order_task_detail)，然后里面有我们这次修改的数据，然后我们进行回滚(其实就是进行修改回原装态)
        WareOrderTaskEntity taskEntity = orderTaskService.getOrderTaskByOrderSn(orderSn);
        Long id = taskEntity.getId();
        //按照工作单找到所有 没有解锁的库存，进行解锁
        List<WareOrderTaskDetailEntity> entities = orderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>().
                eq("task_id", id).eq("lock_status", 1));

        for (WareOrderTaskDetailEntity entity : entities) {
            unLockStock(entity.getSkuId(), entity.getWareId(),
                    entity.getSkuNum(), entity.getId());
        }

    }

    @Data
    class SkuWareHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareId;
    }

}