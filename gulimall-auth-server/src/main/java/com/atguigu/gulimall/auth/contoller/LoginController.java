package com.atguigu.gulimall.auth.contoller;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.feign.MemberFeignService;
import com.atguigu.gulimall.auth.feign.ThirdPartFeignService;
import com.atguigu.common.to.MemberEntity;
import com.atguigu.gulimall.auth.vo.UserLoginVo;
import com.atguigu.gulimall.auth.vo.UserRegistVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author:luosheng
 * @Date:2023-05-26 13:23
 * @Description:
 */
@Controller
public class LoginController {

    @Autowired
    ThirdPartFeignService thirdPartFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    MemberFeignService memberFeignService;

    //跳转登录页
    @GetMapping("login.html")
    public String loginPage(HttpSession session) {
        Object attribute = session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (attribute != null) {
            return "redirect:http://gulimall.com";
        }
        return "login";
    }


    /**
     * 发送一个请求直接跳转到一个页面
     * SpringMVC viewcontroller: 将请求合页面映射过来
     */
    @ResponseBody
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone) {
        //TODO //1.接口防刷
        String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (!StringUtils.isEmpty(redisCode)) {
            long l = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - l < 60000) {
                //60秒内不能再发
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(), BizCodeEnum.SMS_CODE_EXCEPTION.getMsg());
            }
        }

        //2.验证码的再次校验。redis.存入key-phone,value-code   键 sms:code:17513263068 值 xx45645,然后然他存十分钟
        //可以在值后跟一个时间
        //     String code = UUID.randomUUID().toString().substring(0, 5) + "_" + System.currentTimeMillis();
        String code = (int) ((Math.random() * 9 + 1) * 10000) + "_" + System.currentTimeMillis();
        //redis缓存验证码，防止同一个phone在60秒内再次发送验证码
        redisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone, code, 10, TimeUnit.MINUTES);
        code = code.split("_")[0];
        thirdPartFeignService.sendCode(phone, code);
        return R.ok();
    }

    /**
     * //TODO 重定向携带数据，利用session原理。将数据放在session中
     * 只要跳到下一个页面取出这个数据以后，session里面的数据就会删掉
     * <p>
     * //TODO 1.分布式下的session问题
     * RedirectAttributes redirectAttributes  模拟重定向携带数据
     *
     * @param vo
     * @param result
     * @param redirectAttributes
     * @return
     */
    @PostMapping("/regist")
    public String regist(@Valid UserRegistVo vo, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream().
                    collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (v1, v2) -> v1));
            //比如我们把phonexxx 作为键 错误信息为值，这时候可能会出现键冲突的问题 Duplicate key 这时候使用  (v1,v2)->v2) 覆盖掉上一个键，也可以转为list集合

            //  model.addAttribute("errors", errors);
            //这里转发会出现一个问题
            //我们用户注册——>/regist[post] ---->转发reg.html(这里它默认路径映射都是get方式访问的)
            redirectAttributes.addFlashAttribute("errors", errors);//添加个一个一闪而过的属性
            //如果有问题，转发到注册页
            return "redirect:http://auth.gulimall.com/reg.html";
        }

        //真正注册，调用远程服务
        //校验验证码
        String code = vo.getCode();
        String s = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
        if (!StringUtils.isEmpty(s)) {
            String s1 = s.split("_")[0];
            String s2 = s.split("_")[0];
            if (s2.equals(s1)) {
                //删除验证码
                redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
                //验证码正确,调用远程服务进行注册
                R regist = memberFeignService.regist(vo);
                if (regist.getCode() == 0) {
                    //成功
                    return "redirect:http://auth.gulimall.com/login.html";
                } else {
                    //失败
                    Map<String, String> errors = new HashMap<>();
                    errors.put("msg", regist.getData("msg", new TypeReference<String>() {
                    }));
                    redirectAttributes.addFlashAttribute("errors", errors);
                    return "redirect:http://auth.gulimall.com/reg.html";
                }

            } else {
                //验证码不正确
                Map<String, String> errors = new HashMap<>();
                errors.put("errors", "验证码不正确");
                redirectAttributes.addFlashAttribute("errors", errors);
                return "redirect:http://auth.gulimall.com/reg.html";
            }
        } else {
            //验证码过期
            Map<String, String> errors = new HashMap<>();
            errors.put("errors", "验证码已过期");
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }

        //注册成功回到登录页
//        return "redirect:/login.html";
    }


    @PostMapping("/login")
    public String login(UserLoginVo vo, RedirectAttributes redirectAttributes, HttpSession session) {
        //远程登录
        R login = memberFeignService.login(vo);
        if (login.getCode() == 0) {
            //成功
            MemberEntity data = login.getData("data", new TypeReference<MemberEntity>() {
            });
            session.setAttribute(AuthServerConstant.LOGIN_USER, data);
            return "redirect:http://gulimall.com";
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("msg", login.getData("msg", new TypeReference<String>() {
            }));
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/login.html";
        }

    }


}
