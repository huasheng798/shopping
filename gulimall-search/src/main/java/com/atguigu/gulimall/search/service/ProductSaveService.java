package com.atguigu.gulimall.search.service;

import com.atguigu.common.to.es.SkuEsModel;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * @Author:luosheng
 * @Date:2023-05-16 12:11
 * @Description:
 */

public interface ProductSaveService {

    boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;
}
