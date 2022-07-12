package com.example.seckill.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

//公共对象枚举类，响应码和对应的信息
@Getter
@ToString
@AllArgsConstructor
public enum RespBeanEnum {

    //通用
    SUCCESS(200, "SUCCESS"),   //相当于给成员变量赋值
    ERROR(500, "ERROR"),


    //登入模块
    LOGIN_ERROR(500210, "用户名或密码不正确"),
    MOBILE_ERROR(500211, "手机号格式不正确"),
    BIND_ERROR(500212, "参数异常错误"),
    MOBILE_NOT_EXIT(500213, "手机号码不存在"),
    PASSWORD_UPDATE_FAIL(500214, "更新密码失败"),
    SESSION_ERROR(500215, "用户不存在"),

    //订单模块
    ORDER_NOT_EXITS(500300, "订单信息不存在"),

    //秒杀模块
    EMPTY_STOCK(500500, "库存不足"),
    REPEAT_ERROR(500501, "该商品没人限购一件"),
    REQUEST_ILLEGAL(500502, "请求非法，请重新尝试"),
    ERROR_CAPTCHA(500503, "验证码出错，请重新输入"),
    ACCESS_LIMIT_REACHED(500504, "访问过于频繁，请稍后再试")
    ;


    private final Integer code;
    private final String message;


}
