package com.ycw.seckill.service;

import com.ycw.seckill.domain.MiaoshaOrder;
import com.ycw.seckill.domain.MiaoshaUser;
import com.ycw.seckill.domain.OrderInfo;
import com.ycw.seckill.vo.GoodsVo;

/**
 * @author ycw
 */
public interface IOrderService {
    MiaoshaOrder getMiaoshaOrderByUserIdGoodsId(String mobile, Long goodsId);

    OrderInfo createOrder(MiaoshaUser user, GoodsVo goods);

    OrderInfo getMiaoshaOrderByOrderId(Long orderId);
}
