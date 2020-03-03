package com.ycw.seckill.vo;

import com.ycw.seckill.domain.MiaoshaUser;
import lombok.Getter;
import lombok.Setter;

/**
 * @author ycw
 */
@Setter
@Getter
public class GoodsDetailVo {
    private MiaoshaUser user;
    private GoodsVo goods;
    private int miaoshaStatus;
    private int remainSeconds;
}
