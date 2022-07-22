package com.xxxx.seckill.controller;

import com.xxxx.seckill.pojo.Order;
import com.xxxx.seckill.pojo.SeckillOrder;
import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.service.IGoodsService;
import com.xxxx.seckill.service.IOrderService;
import com.xxxx.seckill.service.ISeckillOrderService;
import com.xxxx.seckill.vo.GoodsVo;
import com.xxxx.seckill.vo.RespBean;
import com.xxxx.seckill.vo.RespBeanEnum;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Author Azrael
 * @Date 2022/7/19 19:36
 * @Version 1.0
 * @Description
 */
@Controller
@RequestMapping("/seckill")
public class seckillController {

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private ISeckillOrderService seckillOrderService;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 秒杀(页面静态化)
     *
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/doSeckill",method = RequestMethod.POST)
    @ResponseBody
    public RespBean doSeckill(Model model, User user, @Param(("goodsId")) Long goodsId){
        if(user == null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        GoodsVo goodsVo = goodsService.findGodsVoByGoodsId(goodsId);
        if(goodsVo.getStockCount() < 1){
            return RespBean.error(RespBeanEnum.EMPTY_STOCK_ERROR);
        }
//        QueryWrapper<SeckillOrder> queryWrapper = new QueryWrapper<SeckillOrder>().eq("user_id", user.getId()).eq("goods_id", goodsId);
//        SeckillOrder seckillOrder = seckillOrderService.getOne(queryWrapper);
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsVo.getId());
        if(seckillOrder != null){
            return RespBean.error(RespBeanEnum.REPEAT_ERROR);
        }
        Order order = orderService.seckill(goodsVo,user);
        return RespBean.success(order);

    }


    /**
     * 秒杀
     * windows 1000*10压测 QPS:236.7/sec
     * Linux 1000*10压测 QPS:509.9/sec
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping("/doSeckill2")
    public String doSeckill2(Model model, User user, @Param(("goodsId")) Long goodsId){
        if(user == null){
            return "login";
        }
        GoodsVo goodsVo = goodsService.findGodsVoByGoodsId(goodsId);
        if(goodsVo.getStockCount() < 1){
            model.addAttribute("errMsg", RespBeanEnum.EMPTY_STOCK_ERROR.getMessage());
            return "seckillFail";
        }
//        QueryWrapper<SeckillOrder> queryWrapper = new QueryWrapper<SeckillOrder>().eq("user_id", user.getId()).eq("goods_id", goodsId);
//        SeckillOrder seckillOrder = seckillOrderService.getOne(queryWrapper);
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsVo.getId());
        if(seckillOrder != null){
            model.addAttribute("errMsg", RespBeanEnum.REPEAT_ERROR.getMessage());
            return "seckillFail";
        }
        Order order = orderService.seckill(goodsVo,user);
        model.addAttribute("user",user);
        model.addAttribute("goods",goodsVo);
        model.addAttribute("order",order);
        return "orderDetail";

    }

}
