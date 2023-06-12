package com.atguigu.gulimall.member.execption;

/**
 * @Author:luosheng
 * @Date:2023-05-28 10:39
 * @Description:
 */
public class UsernameExistException extends RuntimeException{
    public UsernameExistException() {
        super("用户名已经存在");
    }
}
