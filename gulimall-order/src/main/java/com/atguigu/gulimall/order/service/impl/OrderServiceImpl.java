package com.atguigu.gulimall.order.service.impl;

import com.atguigu.common.exception.NoStockException;
import com.atguigu.common.mq.OrderTo;
import com.atguigu.common.mq.SeckillOrderTo;
import com.atguigu.common.to.MemberEntity;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.order.constant.OrderConstant;
import com.atguigu.gulimall.order.dao.OrderItemDao;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.enume.OrderStatusEnum;
import com.atguigu.gulimall.order.feign.CartFeignService;
import com.atguigu.gulimall.order.feign.MemberFeignService;
import com.atguigu.gulimall.order.feign.ProductFeignService;
import com.atguigu.gulimall.order.feign.WmsFeignService;
import com.atguigu.gulimall.order.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.order.service.OrderItemService;
import com.atguigu.gulimall.order.to.OrderCreateTo;
import com.atguigu.gulimall.order.vo.*;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
//import io.seata.spring.annotation.GlobalTransactional;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.fastjson.TypeReference;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.order.dao.OrderDao;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private ThreadLocal<OrderSubmitVo> OrderSubmitVoThreadLocal = new ThreadLocal<>();
    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    WmsFeignService wmsFeignService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    OrderDao orderDao;

    @Autowired
    OrderItemDao orderItemDao;

    @Autowired
    OrderItemService orderItemService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
        MemberEntity memberEntity = LoginUserInterceptor.loginUser.get();
        System.out.println("主线程..." + Thread.currentThread().getId());
        //在主线程拿到共享数据
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        CompletableFuture<Void> getAddressFuture = CompletableFuture.runAsync(() -> {
            //1.远程查询所有的收获地址列表
            System.out.println("member程..." + Thread.currentThread().getId());
            //分别分发给子线程
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> address = memberFeignService.getAddress(memberEntity.getId());
            orderConfirmVo.setAddress(address);
        }, executor);
        CompletableFuture<Void> cartFuture = CompletableFuture.runAsync(() -> {
            System.out.println("cart程..." + Thread.currentThread().getId());
            //分发子线程
            RequestContextHolder.setRequestAttributes(requestAttributes);
            //2.远程查询购物车所有选中的购物项
            List<OrderItemVo> itemVos = cartFeignService.getCurrentUserCartItems();
            orderConfirmVo.setItems(itemVos);
            //在feign的远程调用中会出现一个丢失请求头的问题
            //问题就出自我们feign它是从新发的一个请求，它的请求没有带请求头，到我们cart购物车的哪里它还是没有带任东西，所以就会默认认为我们
            //没有登录，现在主要问题就在于，我们feign，没有自己的请求拦截器
        }, executor).thenRunAsync(() -> {
//查询是否有货无货
            List<OrderItemVo> items = orderConfirmVo.getItems();
            //获取全部商品的id
            List<Long> skuIds = items.stream()
                    .map((itemVo -> itemVo.getSkuId()))
                    .collect(Collectors.toList());

            //远程查询商品库存信息
            R skuHasStock = wmsFeignService.getSkusHasStock(skuIds);
            List<SkuStockVo> skuStockVos = skuHasStock.getData("data", new TypeReference<List<SkuStockVo>>() {
            });

            if (skuStockVos != null && skuStockVos.size() > 0) {
                //将skuStockVos集合转换为map
                Map<Long, Boolean> skuHasStockMap = skuStockVos.stream().
                        collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));
                orderConfirmVo.setStocks(skuHasStockMap);
            }
        }, executor);


        //3.查询用户积分
        Integer integration = memberEntity.getIntegration();
        orderConfirmVo.setIntegration(integration);
        //4.有的那些属性已经计算好了

        //todo 防重令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        //数据库保存一份
        stringRedisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberEntity.getId(), token, 30, TimeUnit.MINUTES);
        //前端页面保存一份
        orderConfirmVo.setOrderToken(token);

        CompletableFuture.allOf(getAddressFuture, cartFuture).get();

        return orderConfirmVo;
    }

    //这个@Transactional只是本地事务，在分布式系统中，只能控制住自己的回滚，控制不了其他服务的回滚
    //分布式事务:最大原因，网络问题+分布式机器
    //@GlobalTransactional//全局事务
    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {
        OrderSubmitVoThreadLocal.set(vo);//先给本地线程保存一份
        SubmitOrderResponseVo response = new SubmitOrderResponseVo();
        response.setCode(0);//默认为0 只要有问题code就会变成其他的错误
        //先从本地线程拿到相关的用户信息
        MemberEntity memberEntity = LoginUserInterceptor.loginUser.get();
        //1.验证令牌【令牌的对比和删除必须保证原子性】
        //要和redis中去对比
        String orderToken = vo.getOrderToken();
        //如果我们get的值等于我们传过来的值（ARGV[1]） 他就会删除令牌 否则删除 删除成功返回1  失败返回0
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        //原子验证令牌和删除令牌
        Long result = stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class),
                Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberEntity.getId()), orderToken);

        if (result == 0L) {
            //验证失败
            response.setCode(1);
            return response;
        } else {
            //令牌验证成功
            //下单：去创建订单，验令牌，验价格，锁库存
            //1.创建订单，订单项等信息
            OrderCreateTo order = createOrder();
            //验价,前端和后台比较价钱
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = vo.getPayPrice();
            //可能前端传来的会丢失精度，我们让他可以小于0.01的差价
            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
                //金额对比成功
                //
                //todo 3.保存订单到数据库
                saveOrder(order);
                //4.库存锁定,只要有异常回滚订单数据(这里需要远程调用库存服务)
                //首先需要订单号，所有的订单项(skuId，skuName，num)
                WareSkuLockVo lockVo = new WareSkuLockVo();
                lockVo.setOrderSn(order.getOrder().getOrderSn());//订单号
                //然后每个订单项的数据需要给他赋过去
                List<OrderItemVo> locks = order.getOrderItems().stream().map(item -> {
                    OrderItemVo itemVo = new OrderItemVo();
                    itemVo.setSkuId(item.getSkuId());
                    itemVo.setCount(item.getSkuQuantity());//数量
                    itemVo.setTitle(item.getSkuName());
                    return itemVo;
                }).collect(Collectors.toList());

                //todo 4.远程锁库存 ，重要操作
                lockVo.setLocks(locks);
                //库存成功了，但是网络原因超时了，订单回滚，库存不滚
                R r = wmsFeignService.orderLockStock(lockVo);
                if (r.getCode() == 0) {
                    //锁成功
                    response.setOrder(order.getOrder());
                    //todo 5.远程扣减积分
//      int i = 10 / 0;//这里会出现个问题，就是订单回滚，库存不滚
                    //上面只是模拟异常
                    //todo 订单成功了发送消息给MQ
                    rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", order.getOrder());
                    return response;
                } else {

//                    response.setCode(3);//库存锁定出现问题
                    //锁定失败
                    String msg = (String) r.get("msg");
                    throw new NoStockException(msg);

                }

            } else {
                response.setCode(2);//自己设置2为金额有问题
            }
            response.setCode(0);
        }

        /*String redisToken = stringRedisTemplate.opsForValue().
                get(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberEntity.getId());
        //然后把他页面传来的和数据库中当中的进行对比
        if (orderToken != null && orderToken.equals(redisToken)) {
            //令牌验证通过(令牌只要一通过就必须删除)
            stringRedisTemplate.delete(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberEntity.getId());
        } else {
            //不通过

        }*/

        return response;
    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        return this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
    }

    //订单关闭 我们有一个延迟队列，如果发来消息调用了一次这个方法，如果过了订单支付的时间，我们就把订单关闭掉
    @Override
    public void closeOrder(OrderEntity entity) {

        //先查询这个当前订单的最新状态
        OrderEntity orderEntity = this.getById(entity.getId());

        if (orderEntity.getStatus() == OrderStatusEnum.CREATE_NEW.getCode()) {
            //待付款  //关单
            OrderEntity update = new OrderEntity();
            update.setId(entity.getId());
            update.setStatus(OrderStatusEnum.CANCLED.getCode());//先更新未关闭状态
            this.updateById(update);
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderEntity, orderTo);
            //我们为了保证业务的正常，我们给库存那边的队列也要发送一个消息，queue已经绑定好了
            try {
                //todo 保证消息一定会发送出去，每一个消息都可以做好日志记录(给数据库保存每一个消息的详细信息)
                rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTo);
            } catch (Exception e) {
                //todo 将没法送成功的消息进行重试发送。
            }
        }
    }

    @Override
    public PayVo getOrderPay(String orderSn) {
        PayVo payVo = new PayVo();
        OrderEntity order = this.getOrderByOrderSn(orderSn);

        BigDecimal decimal = order.getTotalAmount().setScale(2, BigDecimal.ROUND_UP);
        payVo.setTotal_amount(decimal.toString());
        payVo.setOut_trade_no(order.getOrderSn());

        List<OrderItemEntity> order_sn = orderItemService.list(new QueryWrapper<OrderItemEntity>()
                .eq("order_sn", orderSn));
        OrderItemEntity entity = order_sn.get(0);
        payVo.setSubject(entity.getSkuName());
        payVo.setBody(entity.getSkuAttrsVals());
        return payVo;
    }

    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params) {
        MemberEntity memberEntity = LoginUserInterceptor.loginUser.get();
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>().eq("member_id", memberEntity.getId()).orderByDesc("id")
        );
        //我们再保存一份订单项的详情信息进行返回，然后渲染页面
        page.getRecords().stream().map(order -> {
            List<OrderItemEntity> itemEntities = orderItemService.list(
                    new QueryWrapper<OrderItemEntity>().eq("order_sn", order.getOrderSn()));
            order.setItemEntities(itemEntities);
            return order;
        }).collect(Collectors.toList());

        return new PageUtils(page);
    }

    @Override
    public void createSeckillOrder(SeckillOrderTo seckillOrderTo) {
        //TODO 保存订单信息
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(seckillOrderTo.getOrderSn());
        orderEntity.setMemberId(seckillOrderTo.getMemberId());
        orderEntity.setCreateTime(new Date());
        BigDecimal totalPrice = seckillOrderTo.getSeckillPrice().multiply(BigDecimal.valueOf(seckillOrderTo.getNum()));
        orderEntity.setTotalAmount(totalPrice);

        orderEntity.setPayAmount(totalPrice);

        //todo 这里还要远程查询一下地址
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());

        //保存订单
        this.save(orderEntity);

        //保存订单项信息
        OrderItemEntity orderItem = new OrderItemEntity();
        orderItem.setOrderSn(seckillOrderTo.getOrderSn());
        orderItem.setRealAmount(totalPrice);

        orderItem.setSkuQuantity(seckillOrderTo.getNum());

        //保存商品的spu信息
        R spuInfo = productFeignService.getSpuInfoBySkuId(seckillOrderTo.getSkuId());
        SpuInfoVo spuInfoData = spuInfo.getData("data", new TypeReference<SpuInfoVo>() {
        });
        orderItem.setSpuId(spuInfoData.getSpuId());
        orderItem.setSpuName(spuInfoData.getSkuName());
        orderItem.setSpuBrand(spuInfoData.getSkuName());//这里缺少一个brandid
        orderItem.setCategoryId(spuInfoData.getCatalogId());

        //保存订单项数据
        orderItemService.save(orderItem);


        // 保存订单信息
//        OrderEntity orderEntity = new OrderEntity();
//        orderEntity.setOrderSn(seckillOrderTo.getOrderSn());
//        orderEntity.setMemberId(seckillOrderTo.getMemberId());
//        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
//
//        BigDecimal multiply = seckillOrderTo.getSeckillPrice().multiply(new BigDecimal("" + seckillOrderTo.getNum()));
//        orderEntity.setPayAmount(multiply);
//        this.save(orderEntity);
//
//        //todo 保存订单项信息
//        OrderItemEntity orderItemEntity = new OrderItemEntity();
//        orderItemEntity.setOrderSn(seckillOrderTo.getOrderSn());
//        orderItemEntity.setCouponAmount(multiply);
//        //todo 这里需要获取当前SKU的详细信息进行设置  productFeignService.getSpuInfoBySkuId()
//        orderItemEntity.setSkuQuantity(seckillOrderTo.getNum());
//        orderItemService.save(orderItemEntity);
    }

    /**
     * 保存订单数据
     *
     * @param order
     */
    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setModifyTime(new Date());
        this.save(orderEntity);
        List<OrderItemEntity> orderItems = order.getOrderItems();
//        orderItemService.saveBatch(orderItems); 这个东西它有问题和 seata 整合有问题 ，一到这就回滚,更换写法
        for (OrderItemEntity orderItem : orderItems) {
            orderItemService.save(orderItem);
        }
    }

    //准备一个方法创建订单
    private OrderCreateTo createOrder() {
        OrderCreateTo createTo = new OrderCreateTo();
        //生成订单号
        String orderSn = IdWorker.getTimeId(); //这个是个mybatis-plus带的一个工具类
        //创建订单号
        OrderEntity orderEntity = buildOrder(orderSn);
        //2.获取到所有的订单项
        List<OrderItemEntity> itemEntities = buildOrderItems(orderSn);
        //计算价格,积分等相关
        computePrice(orderEntity, itemEntities);
        createTo.setOrder(orderEntity);
        createTo.setOrderItems(itemEntities);
        return createTo;
    }

    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> itemEntities) {
        BigDecimal total = new BigDecimal("0.0");//总价格默认先为0.0 ,主要用于一会叠加

        BigDecimal coupon = new BigDecimal("0.0");//主要用于叠加优惠卷
        BigDecimal integration = new BigDecimal("0.0");//积分减的数量
        BigDecimal promotion = new BigDecimal("0.0");//积分减的数量
        //叠加积分
        BigDecimal gift = new BigDecimal("0.0");
        //叠加成长值
        BigDecimal growth = new BigDecimal("0.0");
        //将每一项的订单，叠加他们的价钱
        for (OrderItemEntity entity : itemEntities) {
            BigDecimal realAmount = entity.getRealAmount();
            coupon = coupon.add(entity.getCouponAmount()); //这些减的钱已经算好了，这里只是计算他们总共减了多少钱
            integration = integration.add(entity.getIntegrationAmount());
            promotion = promotion.add(entity.getPromotionAmount());
            total = total.add(realAmount);
            //这里需要叠加积分，以及成长值
            gift = gift.add(new BigDecimal(entity.getGiftIntegration().toString()));
            growth = growth.add(new BigDecimal(entity.getGiftGrowth().toString()));
        }
        //1.订单价格相关
        orderEntity.setTotalAmount(total);
        //应付总额
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));//总额 +  运费

        orderEntity.setPromotionAmount(promotion);

        //积分优惠
        orderEntity.setIntegration(integration.intValue());
        //优惠卷
        orderEntity.setCouponAmount(coupon);
        //设置积分成长值信息
        orderEntity.setIntegration(gift.intValue());
        orderEntity.setGrowth(growth.intValue());
        //设置为删除状态
        orderEntity.setDeleteStatus(0);
    }

    /**
     * 构建订单项
     *
     * @param orderSn
     * @return
     */
    private OrderEntity buildOrder(String orderSn) {
        MemberEntity memberEntity = LoginUserInterceptor.loginUser.get();
        OrderEntity entity = new OrderEntity();
        entity.setOrderSn(orderSn);//先把订单号塞进去
        entity.setMemberId(memberEntity.getId());//会员id
        OrderSubmitVo orderSubmitVo = OrderSubmitVoThreadLocal.get();
        //获取收货地址信息
        R fare = wmsFeignService.getFare(orderSubmitVo.getAddrId());
        FareVo fareResp = fare.getData(new TypeReference<FareVo>() {
        });
        entity.setFreightAmount(fareResp.getFare());//放个运费金额
        //设置收货人信息
        entity.setReceiverCity(fareResp.getAddress().getCity());
        entity.setReceiverDetailAddress(fareResp.getAddress().getDetailAddress());
        entity.setReceiverName(fareResp.getAddress().getName());
        entity.setReceiverPhone(fareResp.getAddress().getPhone());
        entity.setReceiverPostCode(fareResp.getAddress().getPostCode());
        entity.setReceiverProvince(fareResp.getAddress().getProvince());
        entity.setReceiverRegion(fareResp.getAddress().getRegion());

        //设置订单的相关状态信息
        entity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());//待付款状态
        entity.setAutoConfirmDay(7);
        return entity;
    }

    /**
     * 构建所有订单项数据
     *
     * @param
     * @return
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
//这里这个购物车里面获取的价格就是最新的价格
        List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
        if (currentUserCartItems != null && currentUserCartItems.size() > 0) {
            List<OrderItemEntity> itemEntities = currentUserCartItems.stream().map(cartItem -> {
                OrderItemEntity orderItemEntity = buildOrderItem(cartItem);
                orderItemEntity.setOrderSn(orderSn);
                return orderItemEntity;
            }).collect(Collectors.toList());
            return itemEntities;
        }
        return null;
    }

    /**
     * 构建某一个订单项
     *
     * @param cartItem
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVo cartItem) {
        OrderItemEntity itemEntity = new OrderItemEntity();
        //1.订单信息：订单号
        //2.商品的SPU信息
        Long skuId = cartItem.getSkuId();
        R spuInfoBySkuId = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoVo data = spuInfoBySkuId.getData(new TypeReference<SpuInfoVo>() {
        });
        itemEntity.setSpuId(data.getSpuId());
        itemEntity.setSpuBrand(data.getBrandId().toString());
        itemEntity.setSpuName(data.getSkuName());
        itemEntity.setCategoryId(data.getCatalogId());
        //3.商品sku信息
        itemEntity.setSkuId(cartItem.getSkuId());
        itemEntity.setSkuName(cartItem.getTitle());
        itemEntity.setSkuPic(cartItem.getImage());
        itemEntity.setSkuPrice(cartItem.getPrice());
        //这个需要处理一下
        String skuAttr = StringUtils.collectionToDelimitedString(cartItem.getSkuAttr(), ";");
        itemEntity.setSkuAttrsVals(skuAttr);
        itemEntity.setSkuQuantity(cartItem.getCount());
        //4.优惠信息[这个不写]

        //5.积分信息
        itemEntity.setGiftGrowth(cartItem.getPrice().multiply(
                new BigDecimal(cartItem.getCount().toString())).intValue());//这块这个数据可以转为int
        itemEntity.setGiftIntegration(cartItem.getPrice().multiply(
                new BigDecimal(cartItem.getCount().toString())).intValue());
        //6.订单项的价格信息
        itemEntity.setPromotionAmount(new BigDecimal("0"));
        itemEntity.setCouponAmount(new BigDecimal("0"));
        itemEntity.setIntegrationAmount(new BigDecimal("0"));
        //当前订单项的实金额   总额减去各种优惠
        BigDecimal orign = itemEntity.getSkuPrice().
                multiply(new BigDecimal(itemEntity.getSkuQuantity().toString()));
        BigDecimal subtract = orign.subtract(itemEntity.getCouponAmount().subtract(itemEntity.getPromotionAmount()))
                .subtract(itemEntity.getIntegrationAmount());
        itemEntity.setRealAmount(subtract);

        return itemEntity;
    }


}