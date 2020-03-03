package com.ycw.seckill.service.impl;

import com.ycw.seckill.domain.MiaoshaGoods;
import com.ycw.seckill.mybatis.mapper.GoodsMapper;
import com.ycw.seckill.redis.GoodsKey;
import com.ycw.seckill.service.IGoodsService;
import com.ycw.seckill.service.RedisService;
import com.ycw.seckill.vo.GoodsVo;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author ycw
 */
@Service
public class GoodsServiceImpl implements IGoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private RedisService redisService;

    @Override
    public List<GoodsVo> listGoodsVo() {
        List<GoodsVo> goodsVoList = goodsMapper.listGoodsVo();
        return goodsVoList;
    }

    /**
     * 这个方法暂时做了一点修改：
     * 因为没有查出redis中miaoshaGoods的库存数是在何处存入，所以暂时在此处添加
     *
     *
     * @param goodsId
     * @return
     */
    @Override
    public GoodsVo getByGoodsId(Long goodsId) {
        GoodsVo goodsVo = goodsMapper.getByGoodsId(goodsId);
        return goodsVo;
    }

    @Override
    public boolean reduceStock(GoodsVo goods) {
        MiaoshaGoods gs = new MiaoshaGoods();
        gs.setId(goods.getId());
        int stock = goodsMapper.reduceStock(gs);
        if (stock >= 0) {
            return true;
        } else {
            return false;
        }
    }

}
