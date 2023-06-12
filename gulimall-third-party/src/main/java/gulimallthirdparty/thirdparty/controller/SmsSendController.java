package gulimallthirdparty.thirdparty.controller;

import com.atguigu.common.utils.R;
import gulimallthirdparty.thirdparty.component.SmsComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author:luosheng
 * @Date:2023-05-27 14:30
 * @Description:
 */
@RestController
@RequestMapping("/sms")
public class SmsSendController {

    @Autowired
    SmsComponent smsComponent;


    /**
     * 该接口提供给别的服务使用
     *
     * @param phone 需要的请求参数
     * @param code  需要的请求参数
     * @return
     */
    @GetMapping("/sendcode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code) {
        smsComponent.sendSmsCode(phone, code);
        return R.ok();
    }
}
