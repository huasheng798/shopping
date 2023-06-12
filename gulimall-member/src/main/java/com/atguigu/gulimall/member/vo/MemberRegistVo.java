package com.atguigu.gulimall.member.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * @Author:luosheng
 * @Date:2023-05-28 10:17
 * @Description:
 */
@Data
public class MemberRegistVo {
    private String userName;
    private String password;
    private String phone;
}
