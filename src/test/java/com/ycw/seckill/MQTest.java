package com.ycw.seckill;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ycw
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class MQTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Test
    public void test1() {

//        amqpAdmin.declareExchange(new DirectExchange("seckill.exchange"));
//        amqpAdmin.declareQueue(new Queue("seckill.queue"));
//        amqpAdmin.declareBinding(new Binding("seckill.queue", Binding.DestinationType.QUEUE, "seckill.exchange", "seckill", null));
        Map<Object, Object> map = new HashMap<>();
        map.put("msg", "秒杀的第一条消息");
        map.put("data", Arrays.asList("hello", "秒杀", 123));
        rabbitTemplate.convertAndSend("seckill.exchange", "seckill", map);
    }

    @Test
    public void test2() {
        Map map = (Map) rabbitTemplate.receiveAndConvert("seckill.queue");
        System.out.println(map);
    }

}
