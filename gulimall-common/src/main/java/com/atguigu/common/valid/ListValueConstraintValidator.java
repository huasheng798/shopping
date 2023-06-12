package com.atguigu.common.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

/**
 * @Author:luosheng
 * @Date:2023-05-09 15:05
 * @Description:
 */
public class ListValueConstraintValidator implements ConstraintValidator<ListValue,Integer> {

    private Set<Integer> set=new HashSet<>();
    //初始化方法
    @Override
    public void initialize(ListValue constraintAnnotation) {
      int [] vals= constraintAnnotation.vals();
        for (int val:vals) {
            set.add(val);
        }
    }

    //判断是否校验成功

    /**
     *
     * @param integer 需要校验的值
     * @param constraintValidatorContext 当前环境的上下文信息
     * @return
     */
    @Override
    public boolean isValid(Integer integer, ConstraintValidatorContext constraintValidatorContext) {
        return set.contains(integer);
    }
}
