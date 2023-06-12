package com.atguigu.gulimall.ssoclient.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author:luosheng
 * @Date:2023-05-30 11:16
 * @Description:
 */
@Controller
public class HelloController {

    /**
     * 无需登录就可访问
     *
     * @return
     */
    @ResponseBody
    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }


    @GetMapping("/employees")
    public String employees(Model model) {
        List<String> emps = new ArrayList<>();
        emps.add("张三");
        emps.add("李四");
        model.addAttribute("emps", emps);
        return "list";
    }
}
