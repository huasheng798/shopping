package com.atguigu.gulimall.auth.contoller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.to.MemberEntity;
import com.atguigu.gulimall.auth.utils.GiteeHttpClient;
import com.atguigu.common.to.SocialUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * @Author:luosheng
 * @Date:2023-05-29 9:10
 * @Description:处理社交登录请求
 */
//@ConfigurationProperties(prefix = "spring.gitee")

@Controller
public class OAuth2Controller {


    @Value("${gitee.oauth.clientid}")
    public String client_id;
    @Value("${gitee.oauth.clientsecret}")
    public String client_secret;
    @Value("${gitee.oauth.callback}")
    //todo 这个uri格式转换有问题 还没解决
    public String redirect_uri;//http://auth.gulimall.com/oauth2.0/gitee/tonken//


    @GetMapping("/oauth2.0/gitee/success")
    public String gitee() throws Exception {

//        Map<String, String> map = new HashMap<>();
//        map.put("grant_type", "authorization_code");
//        map.put("code", code);
//        map.put("client_id", "e345a832fe3657b5160bb2602efbed34bf3291c155bd72dc6e1f8333b529c6d5");
//        map.put("redirect_uri", "http://auth.gulimall.com/oauth2.0/gitee/tonken");
//        map.put("client_secret", "42e6bde921c4d59e02149059964cc4f80e0753e23c40f7858ed03624cdfcc533");
        // Step1：获取Authorization Code
        String url = "https://gitee.com/oauth/authorize?response_type=code" +
                "&client_id=" + client_id +
                "&redirect_uri=" + "http://auth.gulimall.com/oauth2.0/gitee/tonken" +
                "&scope=user_info";

        return "redirect:" + url;
    }

    @GetMapping("/oauth2.0/gitee/tonken")
    public String tonken(HttpServletRequest request, HttpServletResponse servletResponse) throws IOException {
        HttpSession session = request.getSession();
        // 得到Authorization Code
        String code = request.getParameter("code");
        // 我们放在地址中的状态码
        String state = request.getParameter("state");
        String uuid = (String) session.getAttribute("state");

        // 验证信息我们发送的状态码
        if (null != uuid) {
            // 状态码不正确，直接返回登录页面
            if (!uuid.equals(state)) {
                return "redirect:http://auth.gulimall.com/login.html";
            }
        }

        // Step2：通过Authorization Code获取Access Token
        String url = "https://gitee.com/oauth/token?grant_type=authorization_code" +
                "&client_id=" + client_id +
                "&client_secret=" + client_secret +
                "&code=" + code +
                "&redirect_uri=" + "http://auth.gulimall.com/oauth2.0/gitee/tonken";
        JSONObject accessTokenJson = GiteeHttpClient.getAccessToken(url);

        // Step3: 获取用户信息
        url = "https://gitee.com/api/v5/user?access_token=" + accessTokenJson.get("access_token");
        JSONObject jsonObject = GiteeHttpClient.getUserInfo(url);

        //拿到能拿到的用户数据,调用第三方服务判断是否为第一次登录，如果第一次登录则注册，(账户就是获取来的id，密码暂时可以拿tonken做密码,其实它这个逻辑问题很大，没密码能保存)
//           JSONObject.parseObject(jsonObject,SocialUser.class);
        SocialUser socialUser = JSONObject.parseObject(String.valueOf(jsonObject), SocialUser.class);
        //1.第一次使用session； 命令浏览器保存卡号。JSESSIONID这个cookie
        //以后浏览器访问那个网站就会带上这个网站的cookie
        //子域之间: gulimall.com   auth.gulimall.com order.gulimall.com
        //发卡的时候（指定域名为父域名），即使是子域系统发的卡，也能让父域直接使用。
        //TODO 1.默认发的令牌。session=dsajkdjl.作用域:当前域(解决子域session共享问题)
        //TODO 2.使用JSON的序列化方式来序列化对象数据到redis中
        //这一块只能手动改造一下
        MemberEntity memberEntity = new MemberEntity();
        extracted(socialUser, memberEntity);
        session.setAttribute(AuthServerConstant.LOGIN_USER, memberEntity);
        return "redirect:http://gulimall.com";
    }

    private void extracted(SocialUser socialUser, MemberEntity memberEntity) {
        memberEntity.setUsername(socialUser.getName());
        memberEntity.setId(socialUser.getId());
        memberEntity.setLevelId(1L);
    }


//    public String gitee(@RequestParam("code") String code) throws Exception {
//
//        Map<String, String> map = new HashMap<>();
//        map.put("grant_type", "authorization_code");
//        map.put("code", code);
//        map.put("client_id", "e345a832fe3657b5160bb2602efbed34bf3291c155bd72dc6e1f8333b529c6d5");
//        map.put("redirect_uri", "http://auth.gulimall.com/oauth2.0/gitee/success");
//        map.put("client_secret", "42e6bde921c4d59e02149059964cc4f80e0753e23c40f7858ed03624cdfcc533");
//
//
//        Map<String, String> header = new HashMap<>();
//        Map<String, String> query = new HashMap<>();
//
//        //1.根据code换取accessToken；
//
//        HttpResponse response = HttpUtils.doPost("http://gitee.com", "/oauth/token", "post", header, query, map);
//
//        //2.处理
//        if (response.getStatusLine().getStatusCode() == 302) {
//            //获取到了accessToken
//           String json = EntityUtils.toString(response.getEntity());
//           SocialUser socialUser = JSON.parseObject(json, SocialUser.class);
//            System.out.println(socialUser);
//
//
//            socialUser.getToken_type();
//
//            //知道当前是那个社交用户
//            //1）、当前用户如果是第一次进网站，自动注册进来(为当前社交用户生成一个会员信息账号，以后这个社交账号就对应指定的会员(也就是保存一个用户))
//            //登录或者注册这个社交用户
//
//        } else {
//            return "redirect:http://auth.gulimall.com/login.html";
//        }
//
//
//        //2.登录成功就跳回首页
//
//
//        return "redirect:http://gulimall.com";
//    }


}
