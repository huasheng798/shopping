package com.atguigu.gulimall.member.execption;

/**
 * @Author:luosheng
 * @Date:2023-05-28 10:39
 * @Description:
 */
public class PhoneExistException extends RuntimeException {
    public PhoneExistException() {
        super("手机号已经存在");
    }
}
