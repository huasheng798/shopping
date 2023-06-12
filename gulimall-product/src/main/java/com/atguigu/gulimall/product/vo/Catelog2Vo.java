package com.atguigu.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author:luosheng
 * @Date:2023-05-16 16:46
 * @Description:
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Catelog2Vo {
    private String catalog1Id;//一级父分类id
    private List<Catelog3Vo> catalog3List;//三级子分类
    //当前节点的id和name
    private String id;
    private String name;


    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Catelog3Vo {
        private String catalog2Id;//父分类，2级分类id
        private String id;
        private String name;
    }
}
