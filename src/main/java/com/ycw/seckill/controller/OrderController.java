package com.ycw.seckill.controller;

import com.ycw.seckill.common.enums.ResultStatus;
import com.ycw.seckill.domain.MiaoshaUser;
import com.ycw.seckill.domain.OrderInfo;
import com.ycw.seckill.service.IGoodsService;
import com.ycw.seckill.service.IOrderService;
import com.ycw.seckill.vo.GoodsVo;
import com.ycw.seckill.vo.OrderDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author ycw
 */
@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private IOrderService orderService;

    @Autowired
    private IGoodsService goodsService;

    @RequestMapping("/detail")
    @ResponseBody
    public ResultGeekQ<OrderDetailVo> getOrderDetail(MiaoshaUser user, @RequestParam("orderId") Long orderId, Model model) {

        ResultGeekQ<OrderDetailVo> result = ResultGeekQ.build();
        if (user == null) {
            result.withError(ResultStatus.SESSION_ERROR.getCode(), ResultStatus.SESSION_ERROR.getMsg());
            return result;
        }

        OrderInfo order = orderService.getMiaoshaOrderByOrderId(orderId);
        GoodsVo goods = goodsService.getByGoodsId(order.getGoodsId());
        OrderDetailVo orderDetailVo = new OrderDetailVo();
        orderDetailVo.setGoods(goods);
        orderDetailVo.setOrder(order);
        result.setData(orderDetailVo);

        return result;
    }
}
