package com.example.seckill.controller;


import com.example.seckill.service.IUserService;
import com.example.seckill.vo.LoginVo;
import com.example.seckill.vo.RespBean;
import jdk.nashorn.internal.ir.RuntimeNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

//本身controller就是管理Service层，而Service层是真正的业务层来处理业务逻辑的

//@RestController 会默认将所有方法返回值加上ResponseBody
@Controller
@RequestMapping("/login")
@Slf4j   //日志注解，用于方便打印日志
public class LoginController {

    @Autowired
    private IUserService userService;


    @RequestMapping("/toLogin")
    public String toLogin() {
        return "login";
    }

    @RequestMapping("/doLogin")
    @ResponseBody
    //@Valid使用这个注解，需要引入spring-boot-starter-validation依赖，这样可以自动对loginVo进行校验
    //很纳闷前端怎么会把登入数据封装在LoginVo对象中呢？
    //前端传来的不是一个完整的对象，因此LoginVo前面不用加@RequestBody
    public RespBean doLogin( @Valid LoginVo loginVo, HttpServletRequest request, HttpServletResponse response) {
//        log.info("{}", loginVo);
        //本身controller就是管理Service层，而Service层是真正的业务层来处理业务逻辑的
        RespBean r = userService.doLogin(loginVo, request, response);
        return r;  //这边controller将前端传来的登入信息，跳转到Service层，由Service层处理,返回值由前端接收，再处理
    }


}
