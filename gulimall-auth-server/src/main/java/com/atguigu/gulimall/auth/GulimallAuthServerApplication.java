package com.atguigu.gulimall.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;


/**
 * 大致解释一下SpringSession
 * 其 原理就是用了一个装饰者模式 就是把我们原始的request，response都包装起来，以后获取做一些操作都是被重写过的
 *
 * 而且他还会自动延期,每次刷新session存储redis中的信息都会重新刷新为三十分钟
 */

@EnableRedisHttpSession //整合redis作为session存储
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class GulimallAuthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallAuthServerApplication.class, args);
    }

}
