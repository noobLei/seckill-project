package com.example.seckill.config;

import com.example.seckill.pojo.User;


/**
 *  拦截器可以提前拿到user，我们将user保存到ThreadLocal中，这样其他代码（UserArgumentResolver）可以拿到这个值
 *
 * @author: LC
 * @date 2022/3/9 4:49 下午
 * @ClassName: UserContext
 */
public class UserContext {

    private static ThreadLocal<User> userThreadLocal = new ThreadLocal<>();

    public static void setUser(User user) {
        userThreadLocal.set(user);
    }

    public static User getUser() {
        return userThreadLocal.get();
    }
}
