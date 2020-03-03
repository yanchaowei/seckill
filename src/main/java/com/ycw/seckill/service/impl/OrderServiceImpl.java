package com.ycw.seckill.service.impl;

import com.ycw.seckill.domain.MiaoshaOrder;
import com.ycw.seckill.domain.MiaoshaUser;
import com.ycw.seckill.domain.OrderInfo;
import com.ycw.seckill.mybatis.mapper.OrderMapper;
import com.ycw.seckill.redis.OrderKey;
import com.ycw.seckill.service.IOrderService;
import com.ycw.seckill.service.RedisService;
import com.ycw.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @author ycw
 */
@Service
public class OrderServiceImpl implements IOrderService {

    @Autowired
    private RedisService redisService;

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public MiaoshaOrder getMiaoshaOrderByUserIdGoodsId(String uid, Long goodsId) {
        MiaoshaOrder miaoshaOrder = redisService.get(OrderKey.getMiaoshaOrderByUidGid, uid + "_" + goodsId, MiaoshaOrder.class);
        return miaoshaOrder;
    }

    @Override
    @Transactional
    public OrderInfo createOrder(MiaoshaUser user, GoodsVo goods) {
        // 构建 orderInfo 插入
        OrderInfo order = new OrderInfo();
        order.setCreateDate(new Date());
        order.setDeliveryAddrId(0L);
        order.setGoodsCount(1);
        order.setGoodsId(goods.getId());
        order.setGoodsName(goods.getGoodsName());
        order.setGoodsPrice(goods.getGoodsPrice());
        order.setOrderChannel(1);
        order.setStatus(0);
        order.setUserId(user.getId());
        orderMapper.insertOrderInfo(order);
        // 构建 miaoshaOrder 插入
        MiaoshaOrder miaoshaOrder = new MiaoshaOrder();
        miaoshaOrder.setGoodsId(goods.getId());
        miaoshaOrder.setOrderId(order.getId());
        miaoshaOrder.setUserId(user.getId());
        orderMapper.insertMiaoshaOrder(miaoshaOrder);

        redisService.set(OrderKey.getMiaoshaOrderByUidGid, user.getNickname() + "_" + goods.getId(), miaoshaOrder);
        return order;
    }

    @Override
    public OrderInfo getMiaoshaOrderByOrderId(Long orderId) {
        OrderInfo order = orderMapper.getByOrderId(orderId);
        return order;
    }

}
