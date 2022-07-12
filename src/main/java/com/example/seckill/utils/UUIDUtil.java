package com.example.seckill.utils;

import java.util.UUID;

/**
 * UUID工具类
 *  功能：生成UUID，作为cookie
 * @author zhoubin
 * @since 1.0.0
 */
public class UUIDUtil {

   public static String uuid() {
      return UUID.randomUUID().toString().replace("-", "");
   }

}