package com.example.seckill.controller;


import com.baomidou.mybatisplus.extension.api.R;
import com.example.seckill.pojo.User;
import com.example.seckill.service.IOrderService;
import com.example.seckill.vo.OrderDetailVo;
import com.example.seckill.vo.RespBean;
import com.example.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * 加油，冲冲冲
 *
 * @author qll
 * @since 2022-06-16
 */
@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private IOrderService orderService;

    /**
     *  订单详情
     * @param user
     * @param orderId
     * @return
     */
    @RequestMapping("/orderDetail")
    @ResponseBody
    public RespBean orderDetail(User user, Long orderId) {
        if(user == null)
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        OrderDetailVo orderDetailVo = orderService.detail(orderId);
        return RespBean.success(orderDetailVo);
    }

}
