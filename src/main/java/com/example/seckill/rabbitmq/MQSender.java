package com.example.seckill.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *   消息发送者
 */
@Service
@Slf4j
public class MQSender {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 利用topic模式， 发送秒杀信息
     * @param message
     */
    public void sendSeckillMessage(String message) {
        log.info("发送消息： " + message);
        rabbitTemplate.convertAndSend("seckillExchange", "seckill.message", message);
    }



//以下全部是熟悉rabbit的用法
//    public void send(Object msg) {
//        log.info("发送消息："+msg);
////        rabbitTemplate.convertAndSend("queue", msg);  //发送给队列
//        rabbitTemplate.convertAndSend("fanoutExchange","",  msg);  //发送给交换机，由交换机再转发给队列
//    }
//
//
//    //direct模式
//    public void send01(Object msg) {   //发消息必须携带路由key = "queue.red"
//        log.info("发送red" + msg);
//        rabbitTemplate.convertAndSend("directExchange", "queue.red", msg);
//    }
//    public void send02(Object msg) {  //发消息必须携带路由key = "queue.green"
//        log.info("发送red" + msg);
//        rabbitTemplate.convertAndSend("directExchange", "queue.green", msg);
//    }
//
//
//    //topic模式
//        public void send03(Object msg) {
//        log.info("发送消息(QUEUE01接收)：" + msg);
//        rabbitTemplate.convertAndSend("topicExchange", "queue.red.message", msg);
//    }
//    public void send04(Object msg) {
//        log.info("发送消息(被两个QUEUE接收)：" + msg);
//        rabbitTemplate.convertAndSend("topicExchange", "green.queue.green.message", msg);
//    }
//
//    //header模式   发送消息设置的头部信息，要和交换机进行匹配
//    public void send05(String msg) {
//        log.info("发送消息(QUEUE01和QUEUE02接收)：" + msg);
//        MessageProperties properties = new MessageProperties();
//        properties.setHeader("color", "red");  //设置头部信息
//        properties.setHeader("speed", "fast");  //设置头部信息
//        Message message = new Message(msg.getBytes(), properties);  //发消息，带上头部信息
//        rabbitTemplate.convertAndSend("headersExchange", "", message);
//    }
//
//    public void send06(String msg) {
//        log.info("发送消息(QUEUE01接收)：" + msg);
//        MessageProperties properties = new MessageProperties();
//        properties.setHeader("color", "red");
//        properties.setHeader("speed", "normal");
//        Message message = new Message(msg.getBytes(), properties);
//        rabbitTemplate.convertAndSend("headersExchange", "", message);
//    }

}
