package com.ycw.seckill.mybatis.mapper;

import com.ycw.seckill.domain.MiaoshaGoods;
import com.ycw.seckill.vo.GoodsVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author ycw
 */
@Mapper
public interface GoodsMapper {

    List<GoodsVo> listGoodsVo();

    GoodsVo getByGoodsId(Long goodsId);

    int reduceStock(MiaoshaGoods gs);
}
