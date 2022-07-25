package com.xxxx.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxxx.seckill.exception.GlobalException;
import com.xxxx.seckill.mapper.OrderMapper;
import com.xxxx.seckill.pojo.Order;
import com.xxxx.seckill.pojo.SeckillGoods;
import com.xxxx.seckill.pojo.SeckillOrder;
import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.service.IGoodsService;
import com.xxxx.seckill.service.IOrderService;
import com.xxxx.seckill.service.ISeckillGoodsService;
import com.xxxx.seckill.service.ISeckillOrderService;
import com.xxxx.seckill.vo.GoodsVo;
import com.xxxx.seckill.vo.OrderDetailVo;
import com.xxxx.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Azrael
 * @since 2022-07-18
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ISeckillGoodsService seckillGoodsService;

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private ISeckillOrderService seckillOrderService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Order seckill(GoodsVo goods, User user) {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //从数据库中去库存数据，不要用前端传输过来的数据
        QueryWrapper<SeckillGoods> queryWrapper = new QueryWrapper<SeckillGoods>().eq("goods_id", goods.getId());
        SeckillGoods seckillGoods = seckillGoodsService.getOne(queryWrapper);
        if (seckillGoods.getStockCount() < 1) {
            valueOperations.set("isStockEmpty:" + seckillGoods.getId(), "0");
            throw new GlobalException(RespBeanEnum.EMPTY_STOCK_ERROR);
        }
        System.out.println("==>库存:" + seckillGoods.getStockCount());
        seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
        System.out.println("==>库存:" + seckillGoods.getStockCount());
//        seckillGoodsService.updateById(seckillGoods);
        seckillGoodsService.update(new UpdateWrapper<SeckillGoods>().set("stock_count",seckillGoods.getStockCount()).eq("goods_id", goods.getId()).gt("stock_count", 0));
        //创建订单
        Order order = new Order();
        order.setUserId(user.getId());
        order.setGoodsId(goods.getId());
        order.setGoodsName(goods.getGoodsName());
        order.setGoodsCount(1);
        order.setGoodsPrice(goods.getSeckillPrice());
        order.setOrderChannel(1);
        order.setCreateDate(new Date());
        orderMapper.insert(order);
        //创建秒杀订单
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setUserId(user.getId());
        seckillOrder.setOrderId(order.getId());
        seckillOrder.setGoodsId(goods.getId());
        seckillOrderService.save(seckillOrder);
        valueOperations.set("order:" + user.getId() + ":" + goods.getId(), seckillOrder, 10, TimeUnit.MINUTES);
        return order;
    }

    @Override
    public OrderDetailVo detail(Long orderId) {
        if (orderId == null) {
            throw new GlobalException(RespBeanEnum.ORDER_NOT_EXIST);
        }
        Order order = orderMapper.selectById(orderId);
        GoodsVo goodsVo = goodsService.findGodsVoByGoodsId(order.getGoodsId());
        OrderDetailVo detailVo = new OrderDetailVo();
        detailVo.setOrder(order);
        detailVo.setGoodsVo(goodsVo);
        return detailVo;
    }
}
