package com.atguigu.gulimall.member.vo;

import lombok.Data;

/**
 * @Author:luosheng
 * @Date:2023-05-29 10:11
 * @Description:
 */
@Data
public class SocialUser {
    private String access_token;
    private String token_type;
    private long expires_in;
    private String refresh_token;
    private String scope;
    private long created_at;
}
