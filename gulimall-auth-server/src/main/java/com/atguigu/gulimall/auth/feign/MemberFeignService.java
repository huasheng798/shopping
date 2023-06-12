package com.atguigu.gulimall.auth.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.vo.UserLoginVo;
import com.atguigu.gulimall.auth.vo.UserRegistVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @Author:luosheng
 * @Date:2023-05-28 14:20
 * @Description:
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {
    //注册功能
    @PostMapping("/member/member/regist")
    R regist(@RequestBody UserRegistVo vo);

    //登录功能
    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo vo);
}
