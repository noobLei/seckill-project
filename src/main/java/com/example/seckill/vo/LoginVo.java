package com.example.seckill.vo;


import com.example.seckill.validator.IsMobile;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

//公共  来接收登入参数
@Data
public class LoginVo {
    @NotNull
    @IsMobile   //这个是自定义的注解
    private String mobile;

    @NotNull
    @Length(min = 32)  //长度限制
    private String password;
}
