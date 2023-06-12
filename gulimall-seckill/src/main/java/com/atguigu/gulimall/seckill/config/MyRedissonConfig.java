package com.atguigu.gulimall.seckill.config;


import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @Author:luosheng
 * @Date:2023-05-18 20:34
 * @Description:主要用于分布式的锁
 */
@Configuration
public class MyRedissonConfig {

    /**
     * 所有对Redisson的使用都是通过RedissonClient对象
     *
     * @return
     * @throws IOException
     */
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redisson() throws IOException {
        //1.创建配置
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.111.100:6379");
        //2.根据Config创建出RedissonClient示例
        RedissonClient redissonClient =  Redisson.create(config);
        return redissonClient;
    }

}
