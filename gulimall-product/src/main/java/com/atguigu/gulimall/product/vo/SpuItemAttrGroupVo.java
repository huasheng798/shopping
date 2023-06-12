package com.atguigu.gulimall.product.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @Author:luosheng
 * @Date:2023-05-24 20:40
 * @Description:
 */
@Data
@ToString
public class SpuItemAttrGroupVo {
    private String groupName;
    private List<Attr> attrs;
}
