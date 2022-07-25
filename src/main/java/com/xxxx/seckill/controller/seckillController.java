package com.xxxx.seckill.controller;

import com.google.gson.Gson;
import com.xxxx.seckill.pojo.SeckillMessage;
import com.xxxx.seckill.pojo.SeckillOrder;
import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.rabbitmq.MQSender;
import com.xxxx.seckill.service.IGoodsService;
import com.xxxx.seckill.service.IOrderService;
import com.xxxx.seckill.service.ISeckillOrderService;
import com.xxxx.seckill.vo.GoodsVo;
import com.xxxx.seckill.vo.RespBean;
import com.xxxx.seckill.vo.RespBeanEnum;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @Author Azrael
 * @Date 2022/7/19 19:36
 * @Version 1.0
 * @Description
 */
@Controller
@RequestMapping("/seckill")
public class seckillController implements InitializingBean {

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private ISeckillOrderService seckillOrderService;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MQSender mqSender;


    private Map<Long,Boolean> emptyStock = new HashMap<>();


    /**
     * 秒杀(页面静态化)
     *
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/doSeckill", method = RequestMethod.POST)
    @ResponseBody
    public RespBean doSeckill(Model model, User user, @Param("goodsId") Long goodsId) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //判断是否重复购买
        SeckillOrder seckillOrder = (SeckillOrder) valueOperations.get("order:" + user.getId() + ":" + goodsId);
        if (seckillOrder != null) {
            return RespBean.error(RespBeanEnum.REPEAT_ERROR);
        }
        //使用内存标记减少Redis访问次数
        if(emptyStock.get(goodsId)){
            return RespBean.error(RespBeanEnum.EMPTY_STOCK_ERROR);
        }
        Long stockCount = valueOperations.decrement("seckillGoods:" + goodsId);
        if(stockCount < 0){
            emptyStock.put(goodsId,true);
            valueOperations.increment("seckillGoods:" + goodsId);
            return RespBean.error(RespBeanEnum.EMPTY_STOCK_ERROR);
        }
        SeckillMessage seckillMessage = new SeckillMessage();
        seckillMessage.setUser(user);
        seckillMessage.setGoodsId(goodsId);
        mqSender.sendSeckilllMessage(new Gson().toJson(seckillMessage));
        return RespBean.success(0);


//        GoodsVo goodsVo = goodsService.findGodsVoByGoodsId(goodsId);
//        if(goodsVo.getStockCount() < 1){
//            return RespBean.error(RespBeanEnum.EMPTY_STOCK_ERROR);
//        }
//        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsVo.getId());
//        if(seckillOrder != null){
//            return RespBean.error(RespBeanEnum.REPEAT_ERROR);
//        }
//        Order order = orderService.seckill(goodsVo,user);
//        return RespBean.success(order);

    }



    @RequestMapping(value = "/getResult",method = RequestMethod.GET)
    @ResponseBody
    public RespBean getResult(User user,@Param("goodsId") Long goodsId){
        if(user == null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        return RespBean.success(seckillOrderService.getResult(user,goodsId));
    }




    /*
     * 秒杀
     *
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
//    @RequestMapping("/doSeckill2")
//    public String doSeckill2(Model model, User user, @Param(("goodsId")) Long goodsId){
//        if(user == null){
//            return "login";
//        }
//        GoodsVo goodsVo = goodsService.findGodsVoByGoodsId(goodsId);
//        if(goodsVo.getStockCount() < 1){
//            model.addAttribute("errMsg", RespBeanEnum.EMPTY_STOCK_ERROR.getMessage());
//            return "seckillFail";
//        }
////        QueryWrapper<SeckillOrder> queryWrapper = new QueryWrapper<SeckillOrder>().eq("user_id", user.getId()).eq("goods_id", goodsId);
////        SeckillOrder seckillOrder = seckillOrderService.getOne(queryWrapper);
//        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsVo.getId());
//        if(seckillOrder != null){
//            model.addAttribute("errMsg", RespBeanEnum.REPEAT_ERROR.getMessage());
//            return "seckillFail";
//        }
//        Order order = orderService.seckill(goodsVo,user);
//        model.addAttribute("user",user);
//        model.addAttribute("goods",goodsVo);
//        model.addAttribute("order",order);
//        return "orderDetail";
//    }
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsVo = goodsService.findGoodsVo();
        if (CollectionUtils.isEmpty(goodsVo)) {
            return;
        }
        goodsVo.forEach(new Consumer<GoodsVo>() {
            @Override
            public void accept(GoodsVo goodsVo) {
                //将商品库存缓存到Redis中
                redisTemplate.opsForValue().set("seckillGoods:" + goodsVo.getId(), goodsVo.getStockCount());
                emptyStock.put(goodsVo.getId(), false);
            }
        });
    }
}
