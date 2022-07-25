package com.xxxx.seckill.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author Azrael
 * @Date 2022/7/24 15:21
 * @Version 1.0
 * @Description
 */
@Service
@Slf4j
public class MQSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendSeckilllMessage(String message){
        log.info("发送消息:" + message);
        rabbitTemplate.convertAndSend("seckillExchange","seckill.message",message);
    }

}
