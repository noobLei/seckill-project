package com.example.seckill.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ配置类-Headers配置类  了解，实际工作中不常用
 *
 * @author: LC
 * @date 2022/3/8 5:39 下午
 * @ClassName: RabbitMQHeadersConfig
 */
@Configuration
public class RabbitMQHeadersConfig {

//    private static final String QUEUE01 = "queue_header01";
//    private static final String QUEUE02 = "queue_header02";
//    private static final String EXCHANGE = "headersExchange";
//
//    @Bean
//    public Queue queue01() {
//        return new Queue(QUEUE01);
//    }
//
//    @Bean
//    public Queue queue02() {
//        return new Queue(QUEUE02);
//    }
//
//    @Bean
//    public HeadersExchange headersExchange() {
//        return new HeadersExchange(EXCHANGE);
//    }
//
//    @Bean
//    public Binding binding01() {
//        Map<String, Object> map = new HashMap<>();
//        map.put("color", "red");
//        map.put("speed", "low");
////        whereAny表示发送者携带的头部信息与本交换机任意一个匹配成功即可
//        return BindingBuilder.bind(queue01()).to(headersExchange()).whereAny(map).match();  //
//    }
//
//    @Bean
//    public Binding binding02() {
//        Map<String, Object> map = new HashMap<>();
//        map.put("color", "red");
//        map.put("speed", "fast");
//        //whereAll表示发送者携带的头部信息与本交换机设置的头部信息要完全匹配
//        return BindingBuilder.bind(queue02()).to(headersExchange()).whereAll(map).match();
//    }
}
