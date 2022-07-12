package com.example.seckill.utils;


import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

/**
 * MD5工具类
 */
@Component
public class MD5Util {

    //Md5加密 函数
    public static String md5(String src) {
        return  DigestUtils.md5Hex(src);
    }
    private static final String salt = "1a2b3c4d";   //加密用的salt盐

    //其实第一次加密，客户端那边已经处理好了，这里后端再写一次是为了更加安全，就是将前端加密过的密码，再次进行加密
    //这个加密的盐是我们自定义的。
    public static String inputPassToFromPass(String inputPass) {
        String str = "" + salt.charAt(0) + salt.charAt(2) + inputPass + salt.charAt(3) + salt.charAt(5);  //字符串拼接顺序随便写
        return md5(str);
    }
    public static String fromPassToDBPass(String fromPass, String salt) {
        String str = "" + salt.charAt(0) + salt.charAt(2) + fromPass + salt.charAt(3) + salt.charAt(5);
        return md5(str);
    }

    //这个方法才是真正的后端到数据库的MD5加密。二次加密
    public static String inputPassToDBPass(String inputPass, String salt) {
        String fromPass = inputPassToFromPass(inputPass);
        String dbPass = fromPassToDBPass(fromPass, salt);
        return dbPass;
    }

    public static void main(String[] args) {
        System.out.println(inputPassToFromPass("123456"));
        System.out.println(fromPassToDBPass("5291f04c4fa4bfa8fddeabd7d8723be8", "1a2b3c4d"));
        System.out.println(inputPassToDBPass("123456", "1a2b3c4d"));
    }

}
