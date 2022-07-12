package com.example.seckill.config;

import net.sf.jsqlparser.statement.execute.Execute;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



/**
 *   rabbitmq的配置类,  生产者 --》 队列 --- 》消费者
 */
@Configuration
public class RabbitMQConfig {

//    private static final String QUEUE01 = "queue_fanout01";
//    private static final String QUEUE02 = "queue_fanout02";
//    private static final String EXCHANGE = "fanoutExchange";  //交换机
//
//    @Bean     //准备一个队列，因为消息生产者生成的消息经过队列，然后消费者去队列中拿消息
//    public Queue queue() {   //注意正确导包Queue   org.springframework.amqp.core.Queue;
//        return new Queue("queue", true);
//    }
//
//    @Bean
//    public Queue queue1() {
//        return new Queue(QUEUE01);
//    }
//
//    @Bean
//    public Queue queue2() {
//        return new Queue(QUEUE02);
//    }
//
//    @Bean
//    public FanoutExchange fanoutExchange() {
//        return new FanoutExchange(EXCHANGE);
//    }
//
//    @Bean
//    public Binding binging01() {
//        return BindingBuilder.bind(queue1()).to(fanoutExchange());  //把队列1绑定到交换机去
//    }
//    @Bean
//    public Binding binging02() {
//        return BindingBuilder.bind(queue2()).to(fanoutExchange()); //把队列2绑定到交换机去
//    }



}
