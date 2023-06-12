package com.atguigu.gulimall.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;


/**
 * @Author:luosheng
 * @Date:2023-05-19 14:50
 * @Description:
 */


@EnableFeignClients//开启远程调用
@EnableRedisHttpSession
@EnableDiscoveryClient
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class GulimallSearchApplication {
    public static void main(String[] args) {
        SpringApplication.run(GulimallSearchApplication.class, args);
    }
}
