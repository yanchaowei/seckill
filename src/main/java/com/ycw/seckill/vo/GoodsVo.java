package com.ycw.seckill.vo;

import com.ycw.seckill.domain.Goods;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class GoodsVo extends Goods {
	private Long id;
	private String goodsName;
	private String goodsImg;
	private Double goodsPrice;
	private Double miaoshaPrice;
	private Integer stockCount;
	private Date startDate;
	private Date endDate;
}