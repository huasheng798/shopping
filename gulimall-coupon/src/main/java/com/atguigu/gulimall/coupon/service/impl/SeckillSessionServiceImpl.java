package com.atguigu.gulimall.coupon.service.impl;

import com.atguigu.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.atguigu.gulimall.coupon.service.SeckillSkuRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.coupon.dao.SeckillSessionDao;
import com.atguigu.gulimall.coupon.entity.SeckillSessionEntity;
import com.atguigu.gulimall.coupon.service.SeckillSessionService;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Autowired
    SeckillSkuRelationService skuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getLates3DaySession() {

        //计算最近三天
        //查出这三天参与秒杀活动的商品
        List<SeckillSessionEntity> list = this.baseMapper.selectList(new QueryWrapper<SeckillSessionEntity>()
                .between("start_time", startTime(), endTime()));

        if (list != null && list.size() > 0) {
            List<SeckillSessionEntity> collect = list.stream().map(session -> {
                Long id = session.getId();
                //查出sms_seckill_sku_relation表中关联的skuId
                List<SeckillSkuRelationEntity> relationSkus = skuRelationService.list(new QueryWrapper<SeckillSkuRelationEntity>()
                        .eq("promotion_session_id", id));
                session.setRelationSkus(relationSkus);
                return session;
            }).collect(Collectors.toList());
            return collect;
        }

        return null;

        //计算最近三天
//        LocalDate now = LocalDate.now();//当前时间
//        LocalDate plus = now.plusDays(1);//给当前时间加1天
//        LocalDate plus1 = now.plusDays(2);//给当前时间加2天   还需要加上时分秒，这里直接抽取
 /*       List<SeckillSessionEntity> list = this.list(new QueryWrapper<SeckillSessionEntity>().
                between("start_time", startTime(), endTime()));
        //如果查出来了这三天的活动那么我们就返回对应的数据
        if (list != null && list.size() > 0) {
            List<SeckillSessionEntity> collect = list.stream().map(session -> {
                session.getId();//通过活动id查询那些商品
                List<SeckillSkuRelationEntity> relationEntities = skuRelationService.list(new QueryWrapper<SeckillSkuRelationEntity>().eq("promotion_session_id", session.getId()));
                session.setRelationSkus(relationEntities);
                return session;
            }).collect(Collectors.toList());
            return collect;
        }


        return null;*/
    }

    private String startTime() {
        LocalDate now = LocalDate.now();
        LocalTime min = LocalTime.MIN;
        LocalDateTime start = LocalDateTime.of(now, min);

        String format = start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return format;
    }

    private String endTime() {
        LocalDate now = LocalDate.now();
        LocalDate localDate = now.plusDays(2);
        LocalDateTime of = LocalDateTime.of(localDate, LocalTime.MAX);
        String format = of.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return format;
    }

}