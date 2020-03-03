package com.ycw.seckill.rabbitmq;

import com.ycw.seckill.domain.MiaoshaUser;

/**
 * @author ycw
 */
public class MiaoshaMessage {
    private MiaoshaUser user;
    private Long goodsId;

    public MiaoshaUser getUser() {
        return user;
    }

    public void setUser(MiaoshaUser user) {
        this.user = user;
    }

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }
}
