package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author:luosheng
 * @Date:2023-05-13 18:54
 * @Description:
 */
@Data
public class MergeVo {
    /**
     * purchaseId: 1, //整单id
     * * items:[1,2,3,4] //合并项集合
     */
    private Long purchaseId;
    private List<Long> items;
}
