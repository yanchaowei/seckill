package com.ycw.seckill.rabbitmq;

import com.ycw.seckill.service.RedisService;
import com.ycw.seckill.vo.MiaoshaMessageVo;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author ycw
 */
@Service
public class MQSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisService redisService;

    public void sendMiaoshaMessage(MiaoshaMessage message) {
        String msg = redisService.beanToString(message);
        rabbitTemplate.convertAndSend(MQConfig.MIAOSHA_QUEUE, msg);
    }

    public void sendMiaoshaMessageVo(MiaoshaMessageVo vo) {
        String msgStr = redisService.beanToString(vo);
        rabbitTemplate.convertAndSend(MQConfig.MIAOSHA_REGISTER, msgStr);
    }
}
