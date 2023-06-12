package com.atguigu.common.mq;

import lombok.Data;

import java.util.List;

/**
 * @Author:luosheng
 * @Date:2023-06-06 11:03
 * @Description:
 */
@Data
public class StockLockedTo {
    private Long id;//库存工作单的id
    private StockDetailTo detailId;//工作单的详情的所有id
}
