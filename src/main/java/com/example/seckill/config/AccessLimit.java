package com.example.seckill.config;

import javax.xml.bind.Element;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *  限流通用接口注解
 */
@Retention(RetentionPolicy.RUNTIME)  //运行时生效的注解
@Target(ElementType.METHOD)   //作用范围:方法
public @interface AccessLimit {

    int second();

    int maxCount();

    boolean needLogin() default  true;

}
