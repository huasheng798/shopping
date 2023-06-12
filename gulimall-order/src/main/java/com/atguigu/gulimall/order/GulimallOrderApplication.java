package com.atguigu.gulimall.order;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 使用RabbitMQ
 * 1. 引入amqp场景，RabbitAutoConfiguration就会自动生效
 * 2.给容器中自动配置了
 * RabbitTemplate、AmqpAdmin、CachingConnectionFactory、RabbitMessagingTemplate
 * 所有的属性都是 spring.rabbitmq 配置属性都在这里配置
 *
 * @ConfigurationPropertiees(prefix = "spring.rabbitmq")
 * <p>
 * 3. 给配置文件中配置 spring.rabbitmq信息
 * 4.@EnableRabbit: @EnableXxxxx 开启功能
 * <p>
 * Seata控制分布式事务
 * 1)、每一个微服务先必须创建 undo_log
 * 2)、安装事务协调器; seata-server；
 * 3)、整合
 * 1.导入依赖 spring-cloud-starter-alibaba-seata
 * 2.解压并启动seata-server; conf下registry.conf  所有注解中心的配置
 * 2.1、先修改了registry type="nacos"
 * 2.2、然后直接bin包下找到seata.server.bat启动(如果闪退看网上好多解决办法) 这时候去nacos就可以看到我们这个服务
 * 3.所有想要用到分布式事务的微服务使用seata DataSourceProxy代理自己的数据源
 * 4.每个微服务，都必须导入
 * fegistry.con
 * file.conf  vgroup_mapping.{application.name}-fescar-service-group = "default"
 * 5.启动测试分布式事务
 * 6.给分布式大事务的入口标注@GlobalTransactional
 * 7.每一个远程的小事务用 @Transactional
 */
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableRedisHttpSession
@EnableRabbit
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class GulimallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallOrderApplication.class, args);
    }

}
