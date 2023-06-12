package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Author:luosheng
 * @Date:2023-05-13 22:35
 * @Description:
 */
@Data
public class PurchaseDoneVo {

    @NotNull
    private Long id;//采购单id

    private List<PurchaseItemDoneVo> items;//整个采购项
}
