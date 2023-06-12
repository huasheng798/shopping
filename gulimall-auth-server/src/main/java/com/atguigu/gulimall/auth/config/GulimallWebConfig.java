package com.atguigu.gulimall.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Author:luosheng
 * @Date:2023-05-26 20:11
 * @Description:
 */
@Configuration
public class GulimallWebConfig implements WebMvcConfigurer {
    /**
     * 视图映射
     *
     * @param registry
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        /**
         *   @GetMapping("/login.html")
         *     public String login() {
         *         return "login";
         *     }
         *     就比如我们想写一个空的只跳转的页面，我们完全可以使用这个
         */
        //  registry.addViewController("/login.html").setViewName("login");
        /**
         *    @GetMapping("/reg.html")
         *     public String reg() {
         *         return "reg";
         *     }
         *     上面这个就相当于下面这个
         */
        registry.addViewController("/reg.html").setViewName("reg");

    }
}
