package com.example.seckill.vo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

///公共返回对象
@Data
@NoArgsConstructor    //无参构造函数
@AllArgsConstructor      //全参构造函数
public class RespBean {

    private long code;
    private String message;
    private Object obj;

    //成功返回结果
    public static RespBean success() {
        return new RespBean(RespBeanEnum.SUCCESS.getCode(), RespBeanEnum.SUCCESS.getMessage(), null);
    }

    public static RespBean success(Object obj) {
        return new RespBean(RespBeanEnum.SUCCESS.getCode(), RespBeanEnum.SUCCESS.getMessage(),obj);
    }

    //失败返回结果，为什么传入枚举类对象，因为失败的响应码不是固定的，所以传入RespBeanEnum对象
    //错误，得区分哪种错误，因此传入RespBeanEnum对象
    public static RespBean error(RespBeanEnum respBeanEnum) {
        return new RespBean(respBeanEnum.ERROR.getCode(), respBeanEnum.getMessage(), null);
    }
    public static RespBean error(RespBeanEnum respBeanEnum, Object obj) {
        return new RespBean(respBeanEnum.ERROR.getCode(), respBeanEnum.getMessage(), obj);
    }


}
