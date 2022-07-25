package com.xxxx.seckill.rabbitmq;

import com.google.gson.Gson;
import com.xxxx.seckill.pojo.SeckillMessage;
import com.xxxx.seckill.pojo.SeckillOrder;
import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.service.IGoodsService;
import com.xxxx.seckill.service.IOrderService;
import com.xxxx.seckill.vo.GoodsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

/**
 * @Author Azrael
 * @Date 2022/7/24 16:24
 * @Version 1.0
 * @Description
 */
@Service
@Slf4j
public class MQReceiver {

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IOrderService orderService;

    @RabbitListener(queues = "seckillQueue")
    public void receiver(String message) {
        log.info("接受消息:" + message);
        SeckillMessage seckillMessage = new Gson().fromJson(message, SeckillMessage.class);
        User user = seckillMessage.getUser();
        GoodsVo goodsVo = goodsService.findGodsVoByGoodsId(seckillMessage.getGoodsId());
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //判断是否重复抢购
        SeckillOrder seckillOrder = (SeckillOrder) valueOperations.get("order:" + user.getId() + ":" + goodsVo.getId());
        if (seckillOrder != null) {
            return;
        }
        //检查库存
        if (goodsVo.getStockCount() < 1) {
            valueOperations.set("isStockEmpty:" + goodsVo.getId(), "0");
            return;
        }
        //下单
        orderService.seckill(goodsVo, user);
    }

}
