package com.example.seckill.service.impl;

import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.seckill.exception.GlobalException;
import com.example.seckill.mapper.UserMapper;
import com.example.seckill.pojo.User;
import com.example.seckill.service.IUserService;
import com.example.seckill.utils.CookieUtil;
import com.example.seckill.utils.MD5Util;
import com.example.seckill.utils.UUIDUtil;
import com.example.seckill.utils.ValidatorUtil;
import com.example.seckill.vo.LoginVo;
import com.example.seckill.vo.RespBean;
import com.example.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * 加油，冲冲冲
 *
 * @author qll
 * @since 2022-06-14
 */
//UserServiceImpl继承了ServiceImpl，又实现了IUserService接口
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private UserMapper userMapper;  //这里保红线不用管

    @Autowired
    private RedisTemplate redisTemplate;   //RedisConfig中得到RedisTemplate  这是实现分布式session的第二种方法的序列化步骤

    //登入的业务逻辑处理  传入的loginVo包含了手机号和密码
    public RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response) {
        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();

        //由于自定义校验，所以这里就不需要了
//        //不管前端是否对数据进行校验，但是后端一定要进行校验
//        if(StringUtils.isEmpty(mobile) || StringUtils.isEmpty(password)) {
//            return RespBean.error(RespBeanEnum.LOGIN_ERROR);
//        }
//        if(!ValidatorUtil.isMobile(mobile)) {
//            return RespBean.error(RespBeanEnum.MOBILE_ERROR);
//        }

        //根据手机号查询用户信息，数据库中id对应的就是手机号
        User user = userMapper.selectById(mobile);
        //数据库中没找到用户，那么用户不存在
        if(null == user) {
//            return RespBean.error(RespBeanEnum.LOGIN_ERROR);
            //因为自定义异常处理，所以可以使用自定义异常
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }


        //判断密码是否正确  根据前端传过来的加密密码，利用数据库中的盐进行md5，再判断是否和数据库中的密码一致
        if(!MD5Util.fromPassToDBPass(password, user.getSalt()).equals(user.getPassword())) {
//            return RespBean.error(RespBeanEnum.LOGIN_ERROR);
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }

        //生成cookie  第一种方法使用spring session，实现分布式session
//        String ticket = UUIDUtil.uuid();
//        request.getSession().setAttribute(ticket, user);  //用户对象存到session中
//        CookieUtil.setCookie(request, response, "userTicket", ticket);  //设置cookie

        //第二种方法， 将用户信息存放到redis，实现分布式session
        String ticket = UUIDUtil.uuid();
        //将用户信息存入redis中
        redisTemplate.opsForValue().set("user:"+ticket, user);
        CookieUtil.setCookie(request, response, "userTicket", ticket);  //设置cookie
        return  RespBean.success(ticket);
    }


    //根据cookie获取用户信息  userTicket这个就是cookie
    public User getUserByCookie(String userTicket, HttpServletRequest request, HttpServletResponse response) {
        if(userTicket.equals("")) {
            return null;
        }
        User user = (User)redisTemplate.opsForValue().get("user:"+userTicket);  //去redis中查找userTicket对应的value
        if(user!=null) {
            CookieUtil.setCookie(request, response, "userTicket", userTicket);
        }
        return user;
    }

    /**
     * 更新密码，虽然这部分功能本项目没用到，主要出发点是：当数据库中的密码进行修改时，同时也要记得删除redis的旧数据
     * 为了保证缓存和数据库的数据一致性问题
     */
    @Override
    public RespBean updatePassword(String userTicket, String password,HttpServletRequest request, HttpServletResponse response) {

        User user = getUserByCookie(userTicket, request, response);
        if(user == null) {
            throw new GlobalException(RespBeanEnum.MOBILE_NOT_EXIT);
        }
        user.setPassword(MD5Util.inputPassToDBPass(password, user.getSalt()));
        int result = userMapper.updateById(user);
        if(1 == result) {  //如果数据库更新成功，那么删除redis的旧数据，为了保证数据一致性
            redisTemplate.delete("user:"+userTicket);  //删除redis
            return RespBean.success();
        }
        return RespBean.error(RespBeanEnum.PASSWORD_UPDATE_FAIL);
    }
}
