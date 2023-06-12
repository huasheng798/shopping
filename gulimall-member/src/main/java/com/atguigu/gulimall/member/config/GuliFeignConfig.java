package com.atguigu.gulimall.member.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author:luosheng
 * @Date:2023-06-03 18:44
 * @Description:
 */
@Configuration
public class GuliFeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        //给容器中放一个RequestInterceptor 重写apply 在里面给请求设置上自己需要的请求头等信息
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                //1.RequestContextHolder 拿到刚进来的这个请求
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();//老请求
                    if (request != null) {
                        //同步请求头数据，Cookie
                        String cookie = request.getHeader("Cookie");
                        //给新请求同步老请求的cookie
                        requestTemplate.header("Cookie", cookie);
                    }
                }
                System.out.println("feign 在远程调用之前先进行 RequestInterceptor.apply");
            }
        };
    }

}