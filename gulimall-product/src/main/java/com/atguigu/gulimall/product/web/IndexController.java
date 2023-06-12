package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * @Author:luosheng
 * @Date:2023-05-16 15:52
 * @Description:
 */
@Controller
public class IndexController {
    @Autowired
    CategoryService categoryService;

    @Autowired
    RedissonClient redisson;

    @GetMapping({"/", "index.html"})
    public String indexPage(Model model) {
        //TODO 1.查出所有的1级分类
        List<CategoryEntity> categoryEntities = categoryService.getLevel1Categorys();
        model.addAttribute("categorys", categoryEntities);
        return "index";
    }

    //index/catalog.json
    @GetMapping("index/catalog.json")
    @ResponseBody
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        Map<String, List<Catelog2Vo>> catalogJson = categoryService.getCatalogJson();
        return catalogJson;
    }

    @ResponseBody
    @GetMapping("/hello")
    public String hello() {
        //1.获取一把锁，只要锁的名字一样，就是同一把锁
        RLock lock = redisson.getLock("my-lock");

        //2.加锁
        lock.lock();
        try {
            System.out.println("加锁成功,执行业务");
            //模拟业务
            Thread.sleep(30000);
        }catch (Exception e){


        }finally {
            //3.解锁
            System.out.println("释放锁");
            lock.unlock();
        }

        return "hello";
    }
}
