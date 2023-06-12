package com.atguigu.common.mq;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @Author:luosheng
 * @Date:2023-06-06 11:12
 * @Description:
 */
@Data
public class StockDetailTo {
    /**
     * id
     */
    @TableId
    private Long id;
    /**
     * sku_id
     */
    private Long skuId;
    /**
     * sku_name
     */
    private String skuName;
    /**
     * 购买个数
     */
    private Integer skuNum;
    /**
     * 工作单id
     */
    private Long taskId;
    /**
     * 仓库id
     */
    private Long wareId;
    /**
     * 1-已锁定  2-已解锁  3-扣减
     */
    private Integer lockStatus;
}
