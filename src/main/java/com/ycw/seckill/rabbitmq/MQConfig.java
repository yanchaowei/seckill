package com.ycw.seckill.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ycw
 */
@Configuration
public class MQConfig {

    @Autowired
    private AmqpAdmin amqpAdmin;

    public static final String MIAOSHA_QUEUE = "miaosha.queue";
    public static final String MIAOSHA_REGISTER = "miaosha_register";
    public static final String EXCHANGE_TOPIC = "exchange_topic";

    public static final String MIAOSHA_MESSAGE = "miaosha_mess";

    public static final String QUEUE = "queue";


    public static final String TOPIC_QUEUE1 = "topic.queue1";
    public static final String TOPIC_QUEUE2 = "topic.queue2";
    public static final String HEADER_QUEUE = "header.queue";
    public static final String DIRECT_EXCHANGE = "directExchange";
    public static final String TOPIC_EXCHANGE = "topicExchange";
    public static final String FANOUT_EXCHANGE = "fanoutxchange";
    public static final String HEADERS_EXCHANGE = "headersExchange";

    /**
     * direct 模式
     */
    @Bean
    public Queue miaoshaQueue() {
        return new Queue(MIAOSHA_QUEUE, true);
    }

    @Bean
    public Exchange directExchange() {
        return new DirectExchange(DIRECT_EXCHANGE);
    }

    @Bean
    public Binding directBindingSeckill() {
        return new Binding(MIAOSHA_QUEUE, Binding.DestinationType.QUEUE, DIRECT_EXCHANGE, "seckill", null);
    }

    @Bean
    public Queue registerQueue() {
        return new Queue(MIAOSHA_REGISTER, true);
    }

    @Bean
    public Binding directBindingRegister() {
        return new Binding(MIAOSHA_REGISTER, Binding.DestinationType.QUEUE, DIRECT_EXCHANGE, "seckill_register", null);
    }

    /**
     * topic 模式
     */
    @Bean
    public Queue topicQueue1() {
        return new Queue(TOPIC_QUEUE1, true);
    }

    @Bean
    public Queue topicQueue2() {
        return new Queue(TOPIC_QUEUE2, true);
    }

//    @Bean
//    public TopicExchange topicExchange() {
//        return new TopicExchange(TOPIC_EXCHANGE);
//    }
//
//    @Bean
//    public Binding topicBinding1() {
//        return BindingBuilder.bind(topicQueue1()).to(topicExchange()).with("topic.key1");
//    }
//    @Bean
//    public Binding topicBinding2() {
//        return BindingBuilder.bind(topicQueue2()).to(topicExchange()).with("topic.#");
//    }

    /**
     * Fanout 模式
     */
    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange(FANOUT_EXCHANGE);
    }
    @Bean
    public Binding fanoutBing1() {
        return BindingBuilder.bind(topicQueue1()).to(fanoutExchange());
    }
    @Bean
    public Binding fanoutBing2() {
        return BindingBuilder.bind(topicQueue2()).to(fanoutExchange());
    }

    /**
     * Header 模式
     */
    @Bean
    public HeadersExchange headersExchange() {
        return new HeadersExchange(HEADERS_EXCHANGE);
    }

    @Bean
    public Queue headerQueue() {
        return new Queue(HEADER_QUEUE);
    }

    @Bean
    public Binding headerBinding() {
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        return BindingBuilder.bind(headerQueue()).to(headersExchange()).whereAll(map).match();
    }

}
