package com.atguigu.gulimall.member.controller;

import java.util.Arrays;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.atguigu.common.exception.BizCodeEnum;

import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.execption.PhoneExistException;
import com.atguigu.gulimall.member.execption.UsernameExistException;
import com.atguigu.gulimall.member.feign.CouponFeignService;
import com.atguigu.gulimall.member.vo.MemberLoginVo;
import com.atguigu.gulimall.member.vo.MemberRegistVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import com.atguigu.gulimall.member.service.MemberService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;


/**
 * 会员
 *
 * @author luosheng
 * @email luosheng@gmail.com
 * @date 2023-05-02 20:53:38
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    private CouponFeignService couponFeignService;

    //社交登录功能
/*    @PostMapping("/oauth2/login")
    public R oauthLogin(@RequestBody SocialUser socialUser) {
        MemberEntity entity = memberService.login(socialUser);
        if (entity != null) {
            //登录成功
            return R.ok();
        } else {
            //登录失败
            return R.error(BizCodeEnum.LOGINACCT_PASSWORD_INVAILD_EXCEPTION.getCode(),
                    BizCodeEnum.LOGINACCT_PASSWORD_INVAILD_EXCEPTION.getMsg());
        }
    }*/


    //登录功能
    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo vo) {
        MemberEntity entity = memberService.login(vo);
        com.atguigu.common.to.MemberEntity commonMember = new com.atguigu.common.to.MemberEntity();

        BeanUtils.copyProperties(entity, commonMember);
        if (commonMember != null) {
            //登录成功
            return R.ok().setData(commonMember);
        } else {
            //登录失败

            return R.error(BizCodeEnum.LOGINACCT_PASSWORD_INVAILD_EXCEPTION.getCode(),
                    BizCodeEnum.LOGINACCT_PASSWORD_INVAILD_EXCEPTION.getMsg());
        }

    }


    //注册功能
    @PostMapping("/regist")
    public R regist(@RequestBody MemberRegistVo registVo) {

        try {
            memberService.regist(registVo);
        } catch (UsernameExistException usernameExistException) {
            return R.error(BizCodeEnum.USER_EXIST_EXCEPTION.getCode(), BizCodeEnum.USER_EXIST_EXCEPTION.getMsg());
        } catch (PhoneExistException phoneExistException) {
            return R.error(BizCodeEnum.PHONE_EXIST_EXCEPTION.getCode(), BizCodeEnum.PHONE_EXIST_EXCEPTION.getMsg());
        }

        return R.ok();
    }


    /**
     * 假如一个用户，需要返回它的所有的优惠卷信息，那它需要调用我们的用户优惠卷模块,
     * 这里我们的优惠卷全查写在了CouponController中了
     *
     * @return
     */
    @RequestMapping("/coupons")
    public R test() {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("张三");
        R membercoupons = couponFeignService.membercoupons();

        //这个不就是相当于我们给R这个里面添加个map集合的内容，第一个为member，为用户实体信息
        //第二个为优惠卷实体信息 ,这个a其实为所谓，只是让你看到key键为a
        //返回的结果member的这个实体然后xxxx信息,然后就是a这里面他又包括了一个R的返回信息
        return R.ok().put("member", memberEntity).put("a", membercoupons);
    }


    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id) {
        MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member) {
        memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member) {
        memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids) {
        memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
