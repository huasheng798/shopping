package com.atguigu.gulimall.product;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 一.整合MyBatis-Plus
 * 1.导入依赖
 * maven中 版本为 3.2
 * <dependency>
 * <aroupId>com.baomidou</aroupId>
 * <artifactId>mybatis-plus-boot-starter</artifactId>
 * <version>3.2.0</version>
 * </dependency>
 * 2.配置
 * 1）.配置数据源
 * 1.导入数据库的驱动
 * 2.在application.yml中配置数据源相关信息
 * 2).配置MyBatis-Plus
 * 二、逻辑删除
 * 1）、配置全局的逻辑删除规则，在yml或配置文件当中(可省略，就是可以不要)
 * 2)、在3.1.1 mybatis-plus这个版本以下都要加一个逻辑删除的组件Bean
 * 3）、给Bean(实体类)那个字段上逻辑删除注解@TableLogic
 * <p>
 * 三、双端校验之后端校验/JSR303
 * 1）、给Bean添加校验注解：import javax.validation.constraints.xxx并可以在()中定义自己的message提示
 * 2)、开启校验功能@Valid
 * 效果:校验错误以后会有默认的响应
 * 3）、给校验的bean后紧跟一个BindingResult，就可以获取到校验的结果
 * 四.分组校验(多场景的复杂校验)
 * 1).@NotBlank(message = "品牌名必须提交",
 * groups = {UpdateGroup.class,AddGroup.class})
 * 给校验注解标注什么情况需要进行校验
 * 2).@Validated({AddGroup.class})
 * 3).默认没有指定分组的校验注解@NotBlank,在分组校验情况下不生效，只会在@Validated({AddGroup.class})生效
 * 五.自定义校验
 * 1).编写一个自定义的校验注解
 * 2).编写一个自定义的校验器 ConstraintValidator
 * 3).关联自定义的校验器和自定义的校验注解
 *
 * @Documented
 * @Constraint(validatedBy = {ListValueConstraintValidator.class 这里可以指定多个不同的校验器，适配不同类型的校验})
 * @Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
 * @Retention(RetentionPolicy.RUNTIME) ---
 * public @interface ListValue{
 * 六、统一的异常处理
 * 1). @ControllerAdvice 加上对应注解
 * 2. 使用@ExceptionHandler标注方法可以处理的异常
 *
 * 七、模板引擎
 *   1)、thymeleaf-starter：关闭缓存(开发时候可以用)
 *   2)、静态资源都放在static文件夹下就可以按照路径直接访问
 *   3)、页面放在templates下，直接访问
 *   4)、页面修改不重启服务器实时更新
 *       1)、引入dev-tools
 *       2)、关闭thymeleaf的缓存
 *       3)、只需ctrl+F9，或者ctrl+shift+F9(当前页) 就可实现不重启服务而变更页面新的效果
 *
 * 6、整合redis
 *  1.引入data-redis-starter
 *  2.简单配置redis的host等信息
 *  3.使用SpringBoot自动配置好的StringRedisTemplate来操作redis
 *
 *  七.整合redisson作为分布式锁等功能框架
 *    1)、引入依赖  redisson
 *    2.配置redison
 *        MyRedissonConfig给容器中配置一个RedissonClient实例即可
 *    3.使用
 *       参照文档做
 *  八、整合SpringCache简化缓存开发
 *       1.引入依赖
 *         spring-boot-starter-cache,spring-boot-starter-data-redis
 *       2.写配置
 *         1).自动配置了那些
 *            CacheAutoConfiguration会导入 RedisCacheConfiguration
 *            自动配置好了缓存管理器RedisCacheManager
 *         2）配置使用redis作为缓存就暂时ok
 *       3.测试使用缓存
 *            @Cancheable :触发将数据保存到缓存的操作
 *            @CancheEvict: 触发将数据从缓存删除的操作
 *            @CanchePut: 不影响方法执行更新缓存
 *            @Canching: 组合以上多个操作
 *            @CancheConfig: 在类级别共享缓存的相同配置
 *           1) 开启缓存注解 @EnableCaching
 *           2.只需要使用注解就可以完成缓存操作
 *4、原理:
 *     CacheAutoConfiguration  -> RedisCacheConfiguration ->
 *     自动配置了RedisCacheManager -> 初始化所有的缓存
 * 4.Spring-Cache的不足:
 *      1.读模式:
 *          缓存穿透:查询一个null数据。解决 :缓存空数据: ache-null-values=true
 *          缓存击穿:大量并发同时查询一个正好过期的数据， 解决。加锁： sync=true(加锁)
 *          缓存雪崩:指定的缓存的数据在同一时间一下子全部失效:解决 加随机时间
 *      2.写模式 :(缓存与数据库一致)
 *      1.  读写加锁
 *      2. 引入Canal。感知到MySQL的更新去更新数据库
 *      3. 读多写少，直接去数据库查询就行
 *   总结：
 *      常规数据(读多血少，即时性，一致性要求不高的数据); 完全可以使用Spring-Cache ; 写模式(只要缓存的数据有过期时间就足够了)
 *      特殊数据:特殊设计
 */

@EnableRedisHttpSession //开启redis存储session功能
//开启缓存注解
@EnableCaching
//开启远程调用的功能，可以指定扫描包，(也就是说可以不指定也会给我们扫描出来，但就像xml以前的那种*,难道我们有一千个包都要扫描一遍吗？)
@EnableFeignClients(basePackages = "com.atguigu.gulimall.product.feign")
@MapperScan("com.atguigu.gulimall.product.dao")
@SpringBootApplication
@EnableDiscoveryClient
public class GulimallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }

}
