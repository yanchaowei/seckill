package com.ycw.seckill.mybatis.mapper;

import com.ycw.seckill.domain.MiaoshaOrder;
import com.ycw.seckill.domain.OrderInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author ycw
 */
@Mapper
public interface OrderMapper {
    void insertOrderInfo(OrderInfo order);

    void insertMiaoshaOrder(MiaoshaOrder miaoshaOrder);

    OrderInfo getByOrderId(Long orderId);
}
