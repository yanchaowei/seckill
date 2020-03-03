package com.ycw.seckill.rabbitmq;

import com.rabbitmq.client.Channel;
import com.ycw.seckill.domain.MiaoshaOrder;
import com.ycw.seckill.domain.MiaoshaUser;
import com.ycw.seckill.service.*;
import com.ycw.seckill.vo.GoodsVo;
import com.ycw.seckill.vo.MiaoshaMessageVo;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author ycw
 */
@Service
public class MQReceiver {

    @Autowired
    private RedisService redisService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private MiaoshaService miaoshaService;

    @RabbitListener(queues = MQConfig.MIAOSHA_QUEUE)
    public void receive(String message) {
        MiaoshaMessage miaoshaMessage = redisService.stringToBean(message, MiaoshaMessage.class);
        MiaoshaUser user = miaoshaMessage.getUser();
        Long goodsId = miaoshaMessage.getGoodsId();
        GoodsVo goods = goodsService.getByGoodsId(goodsId);
        Integer stock = goods.getGoodsStock();
        if (stock < 0) {
            return;
        }
        // 判断是否秒杀到了
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getNickname(), goodsId);
        if (order != null) {
            return;
        }
        // 若未秒杀到，则进行秒杀
        miaoshaService.miaosha(user, goods);
    }


    @RabbitListener(queues = MQConfig.MIAOSHA_REGISTER)
    public void receiveMiaoshaMessage(Message message, Channel channel) throws IOException {
        String msgStr = new String(message.getBody(), "UTF-8");
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
        MiaoshaMessageVo miaoshaMessageVo = redisService.stringToBean(msgStr, MiaoshaMessageVo.class);
        messageService.insertMessage(miaoshaMessageVo);
    }
}
