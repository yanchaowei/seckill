package com.ycw.seckill.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author ycw
 */
@Setter
@Getter
public class MiaoshaMessageVo implements Serializable {

    private static final long serialVersionUID = -1341750239648941486L;
    private Integer id ;

    private Long userId;

    private String goodId ;

    private Date orderId;

    private Long messageId ;

    private String content ;

    private Date createTime;

    private Integer status ;

    private Date overTime ;

    private Integer messageType ;

    private Integer sendType ;

    private String goodName ;

    private BigDecimal price ;

    private String messageHead ;

}
