package com.atguigu.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.feign.MemberFeignService;
import com.atguigu.gulimall.ware.vo.FareVo;
import com.atguigu.gulimall.ware.vo.MemberAddressVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.ware.dao.WareInfoDao;
import com.atguigu.gulimall.ware.entity.WareInfoEntity;
import com.atguigu.gulimall.ware.service.WareInfoService;
import org.springframework.util.StringUtils;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Autowired
    MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareInfoEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(qw -> {
                queryWrapper.eq("id", key).
                        or().like("name", key).
                        or().like("address", key).
                        or().like("areacode", key);
            });
        }
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public FareVo getFare(Long addrId) {
        //这里需要远程调用查询收货地址
        R info = memberFeignService.info(addrId);
        FareVo fareVo = new FareVo();
        MemberAddressVo memberAddressVo = info.getData("memberReceiveAddress", new TypeReference<MemberAddressVo>() {
        });
        if (memberAddressVo != null) {
            String phone = memberAddressVo.getPhone();
            //因为没有运费数据库中，就随便搞个
            String substring = phone.substring(phone.length() - 1, phone.length());
            BigDecimal bigDecimal = new BigDecimal(substring);
            fareVo.setAddress(memberAddressVo);
            fareVo.setFare(bigDecimal);
            return fareVo;
        }
        return null;
    }

}