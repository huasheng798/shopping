package com.atguigu.gulimall.cart.vo;

import lombok.Data;
import lombok.ToString;

/**
 * @Author:luosheng
 * @Date:2023-05-30 21:39
 * @Description:
 */
@Data
@ToString
public class UserInfoTo {

    private Long userId;
    private String userKey;
    private Boolean tempUser =false;


}
