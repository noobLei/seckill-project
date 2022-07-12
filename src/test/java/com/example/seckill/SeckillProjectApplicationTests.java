package com.example.seckill;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 *      学习redis分布式锁，不用redission， 而是直接使用spring-boot整合的redis  RedisTemplate
 */

@SpringBootTest
class SeckillProjectApplicationTests {

    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    RedisScript<Boolean> redisScript;

    /**
     * 这样写是存在问题的，如果获得锁之后，抛出异常，那么锁得不到释放，那么其他线程一直拿不到锁
     * 解决办法：设置key的时候，加上失效时间,看方法testLock2
     */
    @Test
    void testLock1() {
        ValueOperations valueOperations = redisTemplate.opsForValue();

        //占位，如果key不存在，可以设置成功
        Boolean isLock = valueOperations.setIfAbsent("k1", "v1");
        //如果占位成功,即获得锁
        if(isLock) {
            valueOperations.set("name", "xxxx");
            String name = (String)valueOperations.get("name");
            //int a = 1 / 0 ;
            //操作结束,那么就释放锁
            redisTemplate.delete("k1");
        } else {
            System.out.println("有线程再使用，请稍后再试");
        }
    }

    /**
     * testLock2方法的缺点：
     * 虽然给锁加上失效时间，但是如果线程由于网络波动，运行时间大于锁的失效时间，那么锁会提前释放，其他线程可以拿到锁并执行，
     * 但是当之前的线程执行完毕之后，还会继续释放锁，但是它释放的是别人的锁，这就造成混乱
     *  怎么解决呢？看testLock3方法
     */
    @Test
    public void testLock2() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //给锁添加一个过期时间
        Boolean isLock = valueOperations.setIfAbsent("k1", "v1", 5, TimeUnit.SECONDS);
        if (isLock) {
            valueOperations.set("name", "xxx");
            String name = (String) valueOperations.get("name");
            System.out.println("name=" + name);
            //操作结束，删除锁
            redisTemplate.delete("k1");
        } else {
            System.out.println("有线程在使用，请稍后再试");
        }
    }

    /**
     * testLock3方法：每次获得锁之后修改key的value，当要释放锁之前，先判断value是否是自己之前设置的，如果是，那么就释放锁，如果不是，啥也不做
        但是还是会存在问题：因为获得锁，设置锁，删除锁，3个步骤不是原子操作，
       解决办法：使用Lua脚本，Lua脚本可以原子性的执行多个redis命令
     */
    @Test
    public void testLock3() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String value = UUID.randomUUID().toString();  //随机值
        //如果不存在key，就设置成功
        Boolean isLock = valueOperations.setIfAbsent("k1", value, 5, TimeUnit.SECONDS);
        if (isLock) {
            valueOperations.set("name", "xxx");
            String name = (String) valueOperations.get("name");
            System.out.println("name=" + name);
            //操作结束，删除锁
            System.out.println(valueOperations.get("k1"));
            //执行lua脚本去释放锁
            Boolean result = (Boolean) redisTemplate.execute(redisScript, Collections.singletonList("k1"), value);
            System.out.println(result);
        } else {
            System.out.println("有线程在使用，请稍后再试");
        }
    }
}
