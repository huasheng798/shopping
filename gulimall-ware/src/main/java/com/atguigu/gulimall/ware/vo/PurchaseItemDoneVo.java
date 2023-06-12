package com.atguigu.gulimall.ware.vo;

import lombok.Data;

/**
 * @Author:luosheng
 * @Date:2023-05-13 22:36
 * @Description:采购项目完成
 */
@Data
public class PurchaseItemDoneVo {
    //  {itemId:5,status:4,reason:"无货"}
    private Long itemId;
    private Integer status;
    private String reason;
}
