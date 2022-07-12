package com.example.seckill.controller;

import ch.qos.logback.core.util.StringCollectionUtil;
import com.example.seckill.pojo.Goods;
import com.example.seckill.pojo.User;
import com.example.seckill.service.IGoodsService;
import com.example.seckill.service.IUserService;
import com.example.seckill.vo.DetailVo;
import com.example.seckill.vo.GoodsVo;
import com.example.seckill.vo.RespBean;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.spring5.view.reactive.ThymeleafReactiveViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    IUserService userService;
    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ThymeleafViewResolver thymeleafViewResolver;  //thymeleaf模板

//    /**
//     * 优化前：每次跳转到新的页面都需要进行User对象的校验
//     */
    //跳转到商品列表页面
//    @RequestMapping("/toList")
//    //通过@CookieValue注解拿到名字叫userTicket的cookie
//    public String toList1(HttpServletRequest request, HttpServletResponse response, Model model, @CookieValue("userTicket") String ticket) {
//        if(StringUtils.isEmpty(ticket)) {
//            return "login"; //拿不到cookie，那就跳转到登入页面
//        }
//        //这是第一种方法，从session拿到用户信息
////        User user = (User)session.getAttribute(ticket);  //去session拿到用户对象
//        //这是第二种方法，从redis拿到用户信息
//        User user = userService.getUserByCookie(ticket, request, response);
//        //校验
//        if(null == user) {
//            return "login";
//        }
//        model.addAttribute("user", user);  //把user传到前端去，进行页面渲染
//        return "goodsList";
//    }
//    /**
//     * 优化后：直接传入User对象， 由WebConfig和UserArgumentResolver类进行User的校验，因此这里不需要校验
//     *
//     *   windows 优化前QPS： 1352
//     */
//    @RequestMapping("/toList")
//    public String toList2(Model model, User user) {
//        model.addAttribute("user", user);  //把user传到前端去，进行页面渲染
//        System.out.println(user.toString() + "tolist111");
//        model.addAttribute("goodsList", goodsService.findGoodsVo());
//        return "goodsList";
//    }

    //第三次优化，使用redis做页面缓存
    @RequestMapping(value="/toList", produces = "text/html;charset=utf-8")
    @ResponseBody
    public String toList3(Model model, User user, HttpServletRequest request, HttpServletResponse response) {
        //Redis中获取页面
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String html = (String)valueOperations.get("goodsList");
        if(!StringUtils.isEmpty(html)) {  //如果不为空，直接返回页面
            return html;
        }

        model.addAttribute("user", user);  //把user传到前端去，进行页面渲染
        System.out.println(user.toString() + "tolist111");
        model.addAttribute("goodsList", goodsService.findGoodsVo());

//        如果html为空，手动渲染，并存入Redis，并返回
        WebContext webContext = new WebContext(request, response, request.getServletContext(),
                request.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodsList", webContext);//手动渲染页面
        if(!StringUtils.isEmpty(html)) { //如果不为空，存到redis中
            valueOperations.set("goodsList", html, 60, TimeUnit.SECONDS);
        }
        return html;
//        return "goodsList";
    }

    //最初版
//    @RequestMapping("/toDetail/{goodsId}")
//    public String toDetail1(Model model, User user, @PathVariable Long goodsId) {
//        model.addAttribute("user", user);
//        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
//        Date startDate = goodsVo.getStartDate();
//        Date endDate = goodsVo.getEndDate();
//        Date nowDate = new Date();
//        //秒杀状态
//        int seckillStatus = 0;
//        //秒杀倒计时
//        int remainSeconds = 0;
//        if(nowDate.before(startDate)) { //还未开始进行秒杀
//            remainSeconds = (int)(startDate.getTime() - nowDate.getTime()) / 1000;
//        } else if(nowDate.after(endDate)) {
//            //秒杀结束
//            seckillStatus = 2;
//            remainSeconds = -1;
//        } else {   //秒杀中
//            seckillStatus = 1;
//            remainSeconds = 0;
//        }
//        model.addAttribute("remainSeconds", remainSeconds);
//        model.addAttribute("seckillStatus", seckillStatus);
//        model.addAttribute("goods", goodsVo);
//        return "goodsDetail";
//    }
    //第三次优化：url缓存
//    @RequestMapping(value = "/toDetail/{goodsId}", produces = "text/html;charset=utf-8")
//    @ResponseBody
//    public String toDetail2(Model model, User user, @PathVariable Long goodsId,
//                           HttpServletRequest request, HttpServletResponse response) {
//        ValueOperations valueOperations = redisTemplate.opsForValue();
//        //Redis中获取页面，如果不为空，直接返回页面
//        String html = (String)valueOperations.get("goodsDetail:" + goodsId);  //"goodsDetail:" + goodsId这个是redis的key
//        if(!StringUtils.isEmpty(html)) {  //如果不为空，直接返回页面
//            return html;
//        }
//
//        model.addAttribute("user", user);
//        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
//        Date startDate = goodsVo.getStartDate();
//        Date endDate = goodsVo.getEndDate();
//        Date nowDate = new Date();
//        //秒杀状态
//        int seckillStatus = 0;
//        //秒杀倒计时
//        int remainSeconds = 0;
//        if(nowDate.before(startDate)) { //还未开始进行秒杀
//            remainSeconds = (int)(startDate.getTime() - nowDate.getTime()) / 1000;
//        } else if(nowDate.after(endDate)) {
//            //秒杀结束
//            seckillStatus = 2;
//            remainSeconds = -1;
//        } else {   //秒杀中
//            seckillStatus = 1;
//            remainSeconds = 0;
//        }
//        model.addAttribute("remainSeconds", remainSeconds);
//        model.addAttribute("seckillStatus", seckillStatus);
//        model.addAttribute("goods", goodsVo);
//
//        //如果html为空，那么进行手动渲染，并存入redis
//        WebContext webContext = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
//        html = thymeleafViewResolver.getTemplateEngine().process("goodsDetail", webContext);  //手动渲染  goodsDetail这里是html模板
//        if(!StringUtils.isEmpty(html)) {  //存入redis
//            valueOperations.set("goodsDetail:"+goodsId, html,60, TimeUnit.SECONDS);
//        }
//        return html;
////        return "goodsDetail";
//    }

    //第四次优化：对toDetail进行优化，前端页面静态化，后端返回一个对象， 不需要页面缓存了，
    @RequestMapping("/toDetail/{goodsId}")
    @ResponseBody
    public RespBean toDetail3(User user, @PathVariable Long goodsId) {
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        Date startDate = goodsVo.getStartDate();
        Date endDate = goodsVo.getEndDate();
        Date nowDate = new Date();
        //秒杀状态
        int seckillStatus = 0;
        //秒杀倒计时
        int remainSeconds = 0;
        if(nowDate.before(startDate)) { //还未开始进行秒杀
            remainSeconds = (int)(startDate.getTime() - nowDate.getTime()) / 1000;
        } else if(nowDate.after(endDate)) {
            //秒杀结束
            seckillStatus = 2;
            remainSeconds = -1;
        } else {   //秒杀中
            seckillStatus = 1;
            remainSeconds = 0;
        }
        DetailVo detailVo = new DetailVo();
        detailVo.setUser(user);
        detailVo.setGoodsVo(goodsVo);
        detailVo.setRemainSeconds(remainSeconds);
        detailVo.setSeckillStatus(seckillStatus);
        return RespBean.success(detailVo); //向前端返回一个对象
    }


}
