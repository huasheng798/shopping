package com.atguigu.gulimall.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 1.开启服务注册发现
 *   (配置nacos的注册中心地址)
 */
@EnableDiscoveryClient
///下面exclude就是暂时排除数据源相关配置要不配有的东西还没有定义
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class GulimallGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallGatewayApplication.class, args);
    }


}
