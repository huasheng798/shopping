package com.atguigu.gulimall.member.service.impl;


import com.atguigu.gulimall.member.dao.MemberLevelDao;
import com.atguigu.gulimall.member.entity.MemberLevelEntity;
import com.atguigu.gulimall.member.execption.PhoneExistException;
import com.atguigu.gulimall.member.execption.UsernameExistException;
import com.atguigu.gulimall.member.vo.MemberLoginVo;
import com.atguigu.gulimall.member.vo.MemberRegistVo;
import com.atguigu.gulimall.member.vo.SocialUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.member.dao.MemberDao;
import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    //查询会员等级
    @Autowired
    MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void regist(MemberRegistVo registVo) {
        MemberDao baseMapper = this.baseMapper;
        MemberEntity memberEntity = new MemberEntity();
        //设置默认等级
        //先查出来默认等级
        MemberLevelEntity memberLevelEntity = memberLevelDao.getDefaultLevel();
        memberEntity.setLevelId(memberLevelEntity.getId());

        //检查用户名和手机号是否唯一.为了让controller能感知异常 ，我们可以使用异常机制
        checkPhoneUnique(registVo.getPhone());
        checkUsernameUnique(registVo.getUserName());

        //密码需要加密存储
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode(registVo.getPassword());
        memberEntity.setPassword(encode);

        //其他的默认值

        //设置手机号和用户名
        memberEntity.setMobile(registVo.getPhone());
        memberEntity.setUsername(registVo.getUserName());

        //保存
        baseMapper.insert(memberEntity);
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException {
        MemberDao baseMapper = this.baseMapper;
        Integer integer = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (integer > 0) {
            //说明有了
            throw new PhoneExistException();
        }
        //否则就没事

    }

    @Override
    public void checkUsernameUnique(String username) throws UsernameExistException {

        Integer integer = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", username));
        if (integer > 0) {
            throw new UsernameExistException();
        }
    }

    @Override
    public MemberEntity login(MemberLoginVo vo) {
        String loginacct = vo.getLoginacct();
        String password = vo.getPassword();

        //1.去数据库查询
        MemberDao baseMapper = this.baseMapper;
        //通过用户名或者手机号来验证
        MemberEntity entity = baseMapper.selectOne(new QueryWrapper<MemberEntity>().
                eq("username", loginacct).
                or().eq("mobile", loginacct));
        if (entity == null) {
            //说明用户里面就没有这个人
            return null;
        } else {
            //比较密码
            //1.获取数据库的密码
            String passwordDb = entity.getPassword();

            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            //密码匹配
            boolean matches = passwordEncoder.matches(password, passwordDb);//第一个为明文密码，第二个加密后的密码
            if (matches) {
                //如果匹配成功说明登录成功
                return entity;
            } else {
                return null;
            }
        }
    }

    @Override
    public MemberEntity login(SocialUser socialUser) {
        //登录和注册合并逻辑

        return null;
    }

}