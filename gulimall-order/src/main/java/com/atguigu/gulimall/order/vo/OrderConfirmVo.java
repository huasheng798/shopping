package com.atguigu.gulimall.order.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @Author:luosheng
 * @Date:2023-06-03 16:44
 * @Description:
 */
//订单确认页需要用的数据

@Data
public class OrderConfirmVo {
    @Getter @Setter
    /** 会员收获地址列表 **/
            List<MemberAddressVo> address;

    @Getter @Setter
    /** 所有选中的购物项 **/
            List<OrderItemVo> items;

    /** 发票记录 **/
    @Getter @Setter
    /** 优惠券（会员积分） **/
    private Integer integration;

    /** 防止重复提交的令牌 **/
    @Getter
    @Setter
    private String orderToken;

    @Getter @Setter
    Map<Long,Boolean> stocks;

    public Integer getCount() {
        Integer count = 0;
        if (items != null && items.size() > 0) {
            for (OrderItemVo item : items) {
                count += item.getCount();
            }
        }
        return count;
    }


    /** 订单总额 **/
    //BigDecimal total;
    //计算订单总额
    public BigDecimal getTotal() {
        BigDecimal totalNum = BigDecimal.ZERO;
        if (items != null && items.size() > 0) {
            for (OrderItemVo item : items) {
                //计算当前商品的总价格
                BigDecimal itemPrice = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                //再计算全部商品的总价格
                totalNum = totalNum.add(itemPrice);
            }
        }
        return totalNum;
    }


    /** 应付价格 **/
    //BigDecimal payPrice;
    public BigDecimal getPayPrice() {
        return getTotal();
    }
//    //收获地址列表 需要用到表 user_member_receive_address表
//    List<MemberAddressVo> address;
//
//    //所有选中的购物项
//    List<OrderItemVo> items;
//
//    //发票记录....
//
//    //TODO 这边优惠卷，发票记录暂时直接 只使用一个积分 ，其他的慢慢完善
//    //优惠卷信息
//    private Integer integration;
////    BigDecimal total;//订单总额
//
//    public BigDecimal getTotal() {
//        BigDecimal sum = new BigDecimal("0");
//        if (items != null) {
//            for (OrderItemVo itemVo : items) {
//                //这个总价就是拿数量乘价钱
//                BigDecimal multiply = itemVo.getPrice().multiply(new BigDecimal(itemVo.getCount().toString()));
//                sum = sum.add(multiply);
//            }
//        }
//        return sum;
//    }
//
//    //    BigDecimal payPrice;//应付价格
//    public BigDecimal getPayPrice() {
//        return getTotal();
//    }
//
//    public Integer getCount() {
//        Integer i = 0;
//        if (items != null) {
//            for (OrderItemVo item : items) {
//                i += item.getCount();
//            }
//        }
//        return i;
//    }
//
//    //额外加一个订单的防重令牌
//    String orderToken;
//
//    Map<Long, Boolean> stocks;
}
