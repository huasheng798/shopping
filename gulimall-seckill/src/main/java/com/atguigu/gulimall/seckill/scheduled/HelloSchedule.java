package com.atguigu.gulimall.seckill.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @Author:luosheng
 * @Date:2023-06-07 21:27
 * @Description: 开启定时任务
 * 开启一个定时任务
 */

/**
 * 定时任务
 * 1.@EnableScheduling  开启定时任务
 * 2.@Scheduled  开启一个定时任务
 * 3.自动配置类  TaskSchedulingAutoConfiguration
 * <p>
 * 异步任务
 * 1.@EnableAsync  开启异步任务功能
 * 2.@Async  给希望异步执行的方法上标注
 * <p>
 * 解决  使用异步+定时任务来完成定时任务不阻塞的功能
 */
@EnableAsync
@Component
@EnableScheduling
@Slf4j
public class HelloSchedule {

    /**
     * 在spring中必须由6位组成 不允许第气位
     * 每个星 差不多就是 时分秒日周月
     * 定时任务不应该阻塞,它默认是阻塞的
     * 1.可以让业务运行以异步的方式，自己提交到线程池
     * 2.默认支持定时任务的线程池
     */
//    @Async
//    @Scheduled(cron = "* * * * * ?")
//    public void hello() {
//        log.info("hello..");
//    }
}
