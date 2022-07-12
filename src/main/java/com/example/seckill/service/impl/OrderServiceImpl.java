package com.example.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.seckill.exception.GlobalException;
import com.example.seckill.mapper.OrderMapper;
import com.example.seckill.mapper.SeckillOrderMapper;
import com.example.seckill.pojo.Order;
import com.example.seckill.pojo.SeckillGoods;
import com.example.seckill.pojo.SeckillOrder;
import com.example.seckill.pojo.User;
import com.example.seckill.service.IGoodsService;
import com.example.seckill.service.IOrderService;
import com.example.seckill.service.ISeckillGoodsService;
import com.example.seckill.service.ISeckillOrderService;
import com.example.seckill.utils.MD5Util;
import com.example.seckill.utils.UUIDUtil;
import com.example.seckill.vo.GoodsVo;
import com.example.seckill.vo.OrderDetailVo;
import com.example.seckill.vo.RespBean;
import com.example.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * 加油，冲冲冲
 *
 * @author qll
 * @since 2022-06-16
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    @Autowired
    ISeckillGoodsService seckillGoodsService;
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    ISeckillOrderService seckillOrderService;
    @Autowired
    IGoodsService goodsService;
    @Autowired
    RedisTemplate redisTemplate;


    /**
     * 秒杀过程, 将秒杀库存减少,并提交订单
     *  防止重复抢购：为了解决用户高并发情况下，出现多次购买同一件秒杀商品，
     *  解决办法：将秒杀订单表的用户id和商品id组成唯一索引这样保证同一用户只能秒杀一件商品
     *
     * 如何解决库存超卖问题？
     * 减库存前，先判断库存是否大于0，只有大于0的情况下才会去减库存
     *
     */
    @Transactional  //事务注解
    public Order seckill(User user, GoodsVo goodsVo) {
        ValueOperations valueOperations = redisTemplate.opsForValue();

        //秒杀商品表库存减一
        SeckillGoods seckillGoods = seckillGoodsService.getOne(new QueryWrapper<SeckillGoods>().eq("goods_id", goodsVo.getId()));
        seckillGoods.setStockCount(seckillGoods.getStockCount()-1);

        //只有id是seckillGoods.getId()，并且stock_count(库存)大于0，才会去扣库存
        boolean seckillGoodsResult = seckillGoodsService.update(new UpdateWrapper<SeckillGoods>().setSql("stock_count = " + "stock_count-1")
                .eq("id", seckillGoods.getId()).gt("stock_count", 0));
//        if(!seckillGoodsResult) {  //更新失败,秒杀失败，则返回
//            return null;
//        }
        //这里感觉存在逻辑问题：假设这里的seckillGoods.getStockCount()拿到库存还有，然后其他用户把库存给抢完了，
        //但是这个用户还不知道，因为他去拿库存的时候还有，所以就不会进入if语句，也就不会设置redis的"isStockEmpty"+goodsVo.getId()
//        那么它就会执行下面的生成订单等操作，这显然是错误的
        if(seckillGoods.getStockCount() < 1) {  //没有库存
            //判断是否有库存
            valueOperations.set("isStockEmpty"+goodsVo.getId(), "0"); //这里的0随便设置的
            return null;
        }

        //生成订单
        Order order = new Order();
        order.setUserId(user.getId());
        order.setGoodsId(goodsVo.getId());
        order.setDeliveryAddrId(0L);
        order.setGoodsName(goodsVo.getGoodsName());
        order.setGoodsCount(1);
        order.setGoodsPrice(seckillGoods.getSeckillPrice());  //订单价格设置为秒杀价格
        order.setOrderChannel(1);
        order.setStatus(0);
        order.setCreateDate(new Date());
        orderMapper.insert(order);//向订单表插入一条记录
//        为什么先生成订单，因为秒杀订单中有有个订单id和订单表是做关联的
        //生成秒杀订单
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setUserId(user.getId());
        seckillOrder.setOrderId(order.getId());
        seckillOrder.setGoodsId(goodsVo.getId());
        seckillOrderService.save(seckillOrder);  //什么时候用save,什么时候用insert呢?

        //将秒杀订单存入redis中，这样其他地方要是再去拿订单信息就不需要从数据库中找，直接可以从redis中拿到订单的信息
        //缓存秒杀订单
        redisTemplate.opsForValue().set("order:"+user.getId()+":"+goodsVo.getId(), seckillOrder);
        return order;  //返回订单对象
    }

    /**
     *  订单详情
     * @param orderId
     * @return
     */
    @Override
    public OrderDetailVo detail(Long orderId) {
        if(orderId == null) {
            throw new GlobalException(RespBeanEnum.ORDER_NOT_EXITS);
        }
        Order order = orderMapper.selectById(orderId);
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(order.getGoodsId());
        OrderDetailVo orderDetailVo = new OrderDetailVo();
        orderDetailVo.setGoodsVo(goodsVo);
        orderDetailVo.setOrder(order);
        return orderDetailVo;
    }

    /**
     * 获取秒杀地址,随机生成一个字符串，拼接到前端的url，生成真正的秒杀地址
     */
    @Override
    public String createPath(User user, Long goodsId) {
        String str = MD5Util.md5(UUIDUtil.uuid() + "123456");
        //存放到redis中，进行后续校验
        redisTemplate.opsForValue().set("seckillPath:" + user.getId() + ":" + goodsId, str, 60, TimeUnit.SECONDS);

        return str;
    }

    /**
     * 校验秒杀地址
     * @param user
     * @param goodsId
     * @return
     */
    @Override
    public boolean checkPath(User user, Long goodsId, String path) {
        if(user == null || goodsId<0 || StringUtils.isEmpty(path)) {
            return false;
        }
        //之前已经把path存到redis中，现在取出来，进行校验
        String redisPath = (String) redisTemplate.opsForValue().get("seckillPath:" + user.getId() + ":" + goodsId);

        return path.equals(redisPath);
    }

    /**
     *  校验验证码
     * @param user
     * @param goodsId
     * @param captcha
     * @return
     */
    @Override
    public boolean checkCaptcha(User user, Long goodsId, String captcha) {

        if(StringUtils.isEmpty(captcha) || user == null || goodsId < 0)
            return false;
        String redisCaptcha = (String)redisTemplate.opsForValue().get("captcha:" + user.getId() + ":" + goodsId);
        //比较前端传过来的验证码和保存再redis中的验证码是否一致
        return captcha.equals(redisCaptcha);
    }
}
