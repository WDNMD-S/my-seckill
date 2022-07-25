package com.xxxx.seckill.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author Azrael
 * @Date 2022/7/24 15:12
 * @Version 1.0
 * @Description
 */
@Configuration
public class RabbitMQConfig {

    private static final String QUEUE = "seckillQueue";

    private static final String EXCHANGE = "seckillExchange";

    @Bean(QUEUE)
    public Queue queue(){
        return QueueBuilder.durable(QUEUE).build();
    }

    @Bean(EXCHANGE)
    public Exchange exchange(){
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    @Bean
    public Binding binding(@Qualifier(QUEUE) Queue queue,@Qualifier(EXCHANGE) Exchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with("seckill.#").noargs();
    }

}
