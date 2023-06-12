package com.atguigu.gulimall.coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 一。如何使用nacos做配置中心
 * 1.引入依赖
 *    <dependency>
 *        <groupId>com.alibaba.cloud</groupId>
 *        <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
 *    </dependency>
 * 2.创建一个bootstrap.properties
 *    spring.application.name=gulimall-coupon  //应用的名字 和配置中心的地址
 *    spring-cloud.nacos.config.server-addr=127.0.0.1:8848
 * 3.需要给配置中心默认添加一个 叫数据集(Data Id)gulimall-coupon.properties.默认规则，应用名.properties
 * 4.然后就可以给 应用名.properties添加任何配置
 * 5.动态获取配置
 *   @RefreshScope:动态获取并刷新配置
 *   @Value("${配置项的名}"):获取到配置
 *   如果配置中心和当前应用的配置文件中都配置了相同的项，优先使用配置中心的配置
 *
 * 二。细节部分
 *   1.命名空间:配置隔离
 *     就是开发环境，测试环境，生产环境
 *     默认:public(保留空间);默认新增的所有配置都在public空间
 *     如果像使用那个命名空间，需要在bootstrap.properties;配置上，指定如下配置
 *     spring.cloud.nacos.config.namespace=c0d97c24-424d-4c25-848b-3b4b2df19055
 *     那一大串是自动生成的，我们生产环境
 *   2.配置集:就是所有配置的集合
 *   3.配置集ID:类似文件名
 *    Data ID:xxxxx  差不多就这个
 *   4.配置分组
 *     默认所有的配置集都属于:DEFAULT_GROUP
 *     我们可以设置双十一 六一八 双十二的分组 比如组名 1111，618,1212 等等
 *
 * 项目中的使用:每隔为服务创建自己的命名空间，使用配置分组区分环境，dev，test，prod
 *
 * 三,同时加载多个配置集(详细看bootstrap.properties)
 * 1.微服务任何配置信息，任何配置文件都可以放在配置中心中
 * 2.只需要在bootstrap.properties说明加载配置中心那些配置文件即可
 * 只要以前SpringBoot任何方法从配置文件中获取值，都能使用
 * 而且配置中心有的优先使用配置中心的
 */
@SpringBootApplication
@EnableDiscoveryClient //开启服务与注册的客户端
public class GulimallCouponApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallCouponApplication.class, args);
    }

}
