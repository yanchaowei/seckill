package com.ycw.seckill.service;

import com.ycw.seckill.vo.GoodsVo;

import java.util.List;

/**
 * @author ycw
 */
public interface IGoodsService {
    List<GoodsVo> listGoodsVo();

    GoodsVo getByGoodsId(Long goodsId);

    boolean reduceStock(GoodsVo goods);
}
