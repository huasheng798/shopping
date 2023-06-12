package com.atguigu.gulimall.seckill.interceptor;

import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.to.MemberEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author:luosheng
 * @Date:2023-06-03 16:21
 * @Description:
 */
@Component
public class LoginUserInterceptor implements HandlerInterceptor {
    public static ThreadLocal<MemberEntity> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // /order/order/status/xxxxxxxx
        String uri = request.getRequestURI();

        boolean match = new AntPathMatcher().match("/kill", uri);
        if (match) {
            MemberEntity attribute = (MemberEntity) request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
            if (attribute != null) {
                //我们把所有的登录信息，都放到本地线程一份，以后想用好拿到
                loginUser.set(attribute);
                return true;
            } else {
                //如果没有登录，就不让他进入到这个支付环接
                request.getSession().setAttribute("msg", "请先进行登录");
                response.sendRedirect("http://auth.gulimall.com/login.html");
                return false;
            }
        }
        return true;
    }

}
