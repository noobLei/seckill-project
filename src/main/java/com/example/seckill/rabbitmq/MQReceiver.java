package com.example.seckill.rabbitmq;

import com.example.seckill.pojo.*;
import com.example.seckill.service.IGoodsService;
import com.example.seckill.service.IOrderService;
import com.example.seckill.utils.JsonUtil;
import com.example.seckill.vo.GoodsVo;
import com.example.seckill.vo.RespBean;
import com.example.seckill.vo.RespBeanEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/***’
 *  消息消费者
 */
@Service
@Slf4j
public class MQReceiver {

    @Autowired
    IGoodsService goodsService;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    IOrderService orderService;

    /**
     *
     * 消费者监听消息队列，拿到消息，就去下查（真正的数据库）库存，如果库存够，就下订单
     * @param message
     */
    @RabbitListener(queues = "seckillQueue")  //监听秒杀队列seckillQueue
    public void receive(String message){
        log.info("接收到的消息" + message);
        //拿到秒杀消息
        SeckillMessage seckillMessage = JsonUtil.jsonStr2Object(message, SeckillMessage.class);
        User user = seckillMessage.getUser();
        Long goodsId = seckillMessage.getGoodsId();
        //查找数据库，根据商品id查找商品
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        //查看库存是否够
        if(goodsVo.getStockCount() < 1) {
            return;
        }
        //判断是否重复抢购（为什么还要判断是否重复抢购呢？因为高并发情况下，可能会出现这种情况）
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
        if(seckillOrder != null) {
            return;  //重复抢购
        }

        //下单操作
        orderService.seckill(user, goodsVo);
    }





//以下全部是熟悉rabbit的用法
//    @RabbitListener(queues = "queue")
//    public void receive(Object msg) {
//        log.info("接收消息" + msg);
//    }
//
//    //////////////////////////// fanout模式的交换机
//    @RabbitListener(queues = "queue_fanout01")
//    public void receive01(Object msg) {
//        log.info("接收消息queue_fanout01" + msg);
//    }
//
//    @RabbitListener(queues = "queue_fanout02")
//    public void receive02(Object msg) {
//        log.info("接收消息queue_fanout02" + msg);
//    }
//
////////////////////////////// direct模式的交换机
//    @RabbitListener(queues = "queue_direct01")
//    public void receive03(Object msg) {
//        log.info("QUEUE01接收消息" + msg);
//    }
//    @RabbitListener(queues = "queue_direct02")
//    public void receive04(Object msg) {
//        log.info("QUEUE02接收消息" + msg);
//    }
//
//
//    //////////////////////////// topic模式的交换机
//    @RabbitListener(queues = "queue_topic01")
//    public void receive05(Object msg) {
//        log.info("QUEUE01接收消息" + msg);
//    }
//    @RabbitListener(queues = "queue_topic02")
//    public void receive06(Object msg) {
//        log.info("QUEUE02和QUEUE01两个都接收消息" + msg);
//    }
//
//
//    //////////////////////////// header模式的交换机
//    @RabbitListener(queues = "queue_header01")
//    public void receive07(Message message) {
//        log.info("QUEUE01接收消息 message对象" + message);
//        log.info("QUEUE01接收消息" + new String(message.getBody()));
//    }
//    @RabbitListener(queues = "queue_header02")
//    public void receive08(Message message) {
//        log.info("QUEUE02接收消息 message对象" + message);
//        log.info("QUEUE02接收消息" + new String(message.getBody()));
//    }



}
