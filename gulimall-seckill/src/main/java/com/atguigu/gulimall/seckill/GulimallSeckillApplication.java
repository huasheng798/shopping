package com.atguigu.gulimall.seckill;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 一.整合Sentinel
 * 1）、导入依赖 spring-cloud-starter-alibaba-sentinel
 * 2）、下载sentinel的控制台
 * 3）、配置sentinel控制台地址信息。
 * 4）、在控制台调整参数。【默认所有的留空设置保存内存中，重启失效】
 * 导入这个以后就可以在控制台看到统计信息
 * 二.每一个微服务都导入actuator; 并配合management.endpoints.web.exposure.include=*
 * <p>
 * 四、使用Sentinel来保护feign远程调用：熔断;
 * 1)、调用方的熔断保护：feign.sentinel.enabled=true
 * 2)、调用方手动指定远程服务的降级策略。远程服务被降级处理。触发我们的熔断回调方法
 * 3)、超大浏览的时候，必须牺牲一些远程服务。在服务的提供方(远程服务)指定降级策略。
 * 提供方在运行。但是不运行自己业务逻辑，返回的是默认的熔断数据(限流的数据)
 * 五、自定义受保护的资源
 * 1）、代码方式
 *    try(Entry entry=SphU.entry("seckillSkus")){
 *        //业务逻辑
 *    }catch(Execption e){}
 * 2).基于注解
 *    @SentinelResource(value = "getCurrentSeckillSkusResource", blockHandler = "blockHandler")
 *
 */
@EnableRedisHttpSession
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class GulimallSeckillApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallSeckillApplication.class, args);
    }

}
