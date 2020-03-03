package com.ycw.seckill.vo;

import com.ycw.seckill.domain.OrderInfo;
import lombok.Getter;
import lombok.Setter;

/**
 * @author ycw
 */
@Setter
@Getter
public class OrderDetailVo {

    private GoodsVo goods;
    private OrderInfo order;
}
