package com.example.seckill.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.R;
import com.example.seckill.config.AccessLimit;
import com.example.seckill.exception.GlobalException;
import com.example.seckill.pojo.Order;
import com.example.seckill.pojo.SeckillMessage;
import com.example.seckill.pojo.SeckillOrder;
import com.example.seckill.pojo.User;
import com.example.seckill.rabbitmq.MQSender;
import com.example.seckill.service.IGoodsService;
import com.example.seckill.service.IOrderService;
import com.example.seckill.service.ISeckillOrderService;
import com.example.seckill.utils.JsonUtil;
import com.example.seckill.vo.GoodsVo;
import com.example.seckill.vo.RespBean;
import com.example.seckill.vo.RespBeanEnum;
import com.wf.captcha.ArithmeticCaptcha;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀, 秒杀之后到第三方支付,但是这里不实现
 */
@Slf4j
@Controller
@RequestMapping("/seckill")
public class SecKillController implements InitializingBean {

    @Autowired
    IGoodsService goodsService;
    @Autowired
    ISeckillOrderService seckillOrderService;
    @Autowired
    IOrderService orderService;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    RedisScript<Long> redisScript;
    @Autowired
    MQSender mqSender;
    //空库存map  true：没库存，false：有库存
    private Map<Long, Boolean> EmptyStockMap = new HashMap<>();  //long:商品id， boolean：是否还有库存

    

    //最开始
//    @RequestMapping("/doSecKill")
//    public String doSecKill(Model model, User user, Long goodsId) {
//
//         if(user == null) {
//             return "login";
//         }
//         model.addAttribute("user", user);
//         GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
//
//        //判断库存是否不够
//        if(goodsVo.getStockCount() < 1) {
//            System.out.println("库存剩余量" + goodsVo.getStockCount());
//            model.addAttribute("errmsg", RespBeanEnum.EMPTY_STOCK.getMessage());
//            return "secKillFail";
//        }
//        //判断是否重复抢购 思路:判断当前用户的id和商品id是否和订单表中一样,如果一样说明已经抢了,不能再重复抢
////        这是mybatisplus的写法,就是查找user.getId() 和 goodsId 对应的数据
//        SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId()).eq("goods_id", goodsId));
//        if(seckillOrder != null) {
//            System.out.println("重复抢到");
//            model.addAttribute("errmsg", RespBeanEnum.REPEAT_ERROR.getMessage());
//            return "secKillFail";
//        }
//
//        //否则没有重复抢,那么就提交订单
//        Order order = orderService.seckill(user, goodsVo);
//        model.addAttribute("order", order);
//        model.addAttribute("goods", goodsVo);
//
//        return "orderDetail";
//    }

//---------------------------------****------------------------

//    //秒杀页面静态化，前后端分离，不再用model传数据，而是返回对象，前端利用ajax请求对象
//    @RequestMapping(value = "/doSecKill", method = RequestMethod.POST)
//    @ResponseBody
//    public RespBean doSecKill2(User user, Long goodsId) {
//
//        if(user == null) {
//            return RespBean.error(RespBeanEnum.SESSION_ERROR);
//        }
//        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
//
//        //判断库存是否不够
//        if(goodsVo.getStockCount() < 1) {
//            return RespBean.error(RespBeanEnum.EMPTY_STOCK);  //库存不足
//        }
//
//        //判断是否重复抢购 思路:判断当前用户的id和商品id是否和订单表中一样,如果一样说明已经抢了,不能再重复抢
////        这是mybatisplus的写法,就是查找user.getId() 和 goodsId 对应的数据
////        SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId()).eq("goods_id", goodsId));
//
//        //判断是否重复抢购，orderService已经把订单存入到redis中，因此就不需要像上面代码那样再次查询数据库秒杀订单表了，直接从redis获取秒杀订单的信息
//        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsVo.getId());
//        if(seckillOrder != null) {
//            return RespBean.error(RespBeanEnum.REPEAT_ERROR);  //重复抢购
//        }
//
//        //否则没有重复抢,那么就提交订单
//        Order order = orderService.seckill(user, goodsVo);
//
//        return RespBean.success(order);  //成功
//    }

//---------------------------------****------------------------

    //预减库存：将商品库存数量提前放到redis中，进行预减库存（这样不需要走数据库），
    // 再利用消息队列（rabbitMQ）去下订单，因为用了rabbitmq，所以下单操作变成了异步处理
    @RequestMapping(value = "{path}/doSecKill", method = RequestMethod.POST)
    @ResponseBody
    public RespBean doSecKill3(@PathVariable String path, User user, Long goodsId) {
        if(user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        ValueOperations valueOperations = redisTemplate.opsForValue();

        //这边进行路径校验
        boolean check = orderService.checkPath(user, goodsId, path);
        if(!check) {
            return RespBean.error(RespBeanEnum.REQUEST_ILLEGAL);
        }

        //判断是否重复抢购，从redis中获取订单
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
        if(seckillOrder != null) {
            return RespBean.error(RespBeanEnum.REPEAT_ERROR);  //重复抢购
        }
        //通过内存标记，减少redis的访问，为redis减轻负担
        if(EmptyStockMap.get(goodsId)) {//库存为空
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }

        //预减库存操作，其实是对redis进行预减
//        Long stock = valueOperations.decrement("seckillGoods" + goodsId);
        //使用Lua脚本来执行redis命令，预减库存
        Long stock = (Long) redisTemplate.execute(redisScript, Collections.singletonList("seckillGoods" + goodsId));

        //如果库存不足
        if(stock < 0) {  //为什么不能等于0，因为上面redis将1减为0，此时是存在库存的，所以0是不能参与判断
            EmptyStockMap.put(goodsId, true );  //库存为空
            valueOperations.increment("seckillGoods" + goodsId);  //库存++，为了保证redis的库存不为负数
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        SeckillMessage seckillMessage = new SeckillMessage(user, goodsId);
        mqSender.sendSeckillMessage(JsonUtil.object2JsonStr(seckillMessage));  //给队列发送消息

        return RespBean.success(0);  //成功
    }

    /**
     *  获取秒杀地址 ,现在前端点击秒杀不是直接秒杀，而是获得一个秒杀地址，通过秒杀地址再去秒杀
     *
     *  添加计数器法，来进行限流，就是一分钟能接收同一个ip的几次请求，超过了就停止用户访问, 限制次数也放到redis中，key为用户的请求地址+用户id
     *  限流除了计数器算法，还有漏桶算法，令牌桶算法
     *
     * @return
     */
    @AccessLimit(second = 5, maxCount = 5, needLogin = true)  //自定义注解，功能是：限流
    @RequestMapping(value = "/path", method = RequestMethod.GET)
    @ResponseBody
    public RespBean getPath(User user, Long goodsId, String captcha, HttpServletRequest request) {
        if(user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        ValueOperations valueOperations = redisTemplate.opsForValue();

        //这段计数器限流代码，如果再不同的页面（商品页，详情页等）进行限流，每个地方都要复制这段代码
        //如何通用化呢？将这段代码实现成注解，需要适用的地方直接加注解就好了，
//        //限制访问次数， 限制5妙内访问5次
//        StringBuffer uri = request.getRequestURL();  //来自用户的请求地址
//        captcha = "0";  //为了方便实现，这边把验证码固定了，不然每次都要计算验证码
//        Integer count = (Integer)valueOperations.get(uri + ":" + user.getId());  //
//        if(count == null) {  //第一次访问
//            valueOperations.set(uri + ":" + user.getId(), 1, 5, TimeUnit.SECONDS);  //设置了失效时间，因此一段时间后，还可以继续请求
//        } else if(count < 5){  //如果访问次数小于5
//            valueOperations.increment(uri + ":" + user.getId());
//        } else {  //否则访问次数大于5
//            return RespBean.error(RespBeanEnum.ACCESS_LIMIT_REACHED);
//        }

        //对验证码进行校验
        boolean check = orderService.checkCaptcha(user, goodsId, captcha);
        if(!check) {
            return RespBean.error(RespBeanEnum.ERROR_CAPTCHA);
        }

        //创建接口地址，利用user, goodsId可以确保接口地址的唯一性
        String str = orderService.createPath(user, goodsId);
        return RespBean.success(str);
    }

    /**
     *    生成验证码
     * @param user
     * @param goodsId
     * @param response
     */
    @GetMapping(value = "/captcha")
    public void verifyCode(User user, Long goodsId, HttpServletResponse response) {
        if (user == null || goodsId < 0) {
            throw new GlobalException(RespBeanEnum.REQUEST_ILLEGAL);
        }
        //设置请求头为输出图片的类型
        response.setContentType("image/jpg");
        response.setHeader("Pargam", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        //生成算术验证码
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(130, 32, 3);
        //保存到redis中,一会方便校验
        redisTemplate.opsForValue().set("captcha:" + user.getId() + ":" + goodsId, captcha.text(), 300, TimeUnit.SECONDS);
        try {
            captcha.out(response.getOutputStream());
        } catch (IOException e) {
            log.error("验证码生成失败", e.getMessage());
        }
    }


    /**
     * 获取秒杀结果
     * @param user
     * @param goodsId
     * @return    orderId: 1：成功   -1：秒杀失败    0：排队中
     */
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public RespBean getResult(User user, Long goodsId) {
        if(user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        Long orderId = seckillOrderService.getResult(user, goodsId);
        return RespBean.success(orderId);
    }





    /**
     * 系统初始化时执行的方法， 功能：将商品库存数量提前加载到Redis中
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> list = goodsService.findGoodsVo();
        if(CollectionUtils.isEmpty(list)) {
            return;
        }
        //将商品库存数量加载到redis中
        list.forEach(goodsVo -> {
            redisTemplate.opsForValue().set("seckillGoods"+goodsVo.getId(), goodsVo.getStockCount());
            EmptyStockMap.put(goodsVo.getId(), false);  //初始化，肯定有库存
        });
    }
}
