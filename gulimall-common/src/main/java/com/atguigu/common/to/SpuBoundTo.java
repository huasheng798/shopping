package com.atguigu.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author:luosheng
 * @Date:2023-05-13 10:40
 * @Description: TO包，主要用于模块与模块之间的实体类传输数据...
 * 反正大概就是它也算是个实体类，但它里面的数据，是我们模块与模块之间的调用
 */
@Data
public class SpuBoundTo {
    private Long spuId;
    private BigDecimal buyBounds;
    private BigDecimal growBounds;
}
