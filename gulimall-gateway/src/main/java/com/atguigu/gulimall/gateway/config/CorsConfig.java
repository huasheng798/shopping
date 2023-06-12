package com.atguigu.gulimall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * 功能描述 :  网关统一配置运行跨域
 *
 * @Author:luosheng
 * @Date:2023-05-05 14:52
 * @Description: 配置解决跨 过滤器
 */
@Configuration
public class CorsConfig {


    @Bean
    public CorsWebFilter corsWebFilter() {
        //跨域配置源
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        //跨域配置
        CorsConfiguration configuration = new CorsConfiguration();

        //1.配置跨域
        //允许所有请求头进行跨域
        configuration.addAllowedHeader("*");
        //允许所有请求方式进行跨域
        configuration.addAllowedMethod("*");
        //允许所有请求来源及进行跨域
        configuration.addAllowedOrigin("*");
        //允许携带cookie进行跨域
        configuration.setAllowCredentials(true);
        // 2 任意路径都允许第1步配置的跨域
        source.registerCorsConfiguration("/**", configuration);
        return new CorsWebFilter(source);
    }
}
