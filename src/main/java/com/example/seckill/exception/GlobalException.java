package com.example.seckill.exception;


//定义全局的异常类

import com.example.seckill.vo.RespBeanEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data  //@Data注解的主要作用是提高代码的简洁，使用这个注解可以省去代码中大量的get()、set()、toString()等方法
@NoArgsConstructor
@AllArgsConstructor
public class GlobalException extends RuntimeException{
    RespBeanEnum respBeanEnum;  //成员变量
}
