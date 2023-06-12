package com.atguigu.gulimall.seckill.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Author:luosheng
 * @Date:2023-06-07 21:59
 * @Description:
 */
//开启定时调度
@EnableScheduling
//开启异步
@EnableAsync
@Configuration
public class ScheduledConfig {
}
