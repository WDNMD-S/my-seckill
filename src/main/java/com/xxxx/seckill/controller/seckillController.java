package com.xxxx.seckill.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xxxx.seckill.pojo.Order;
import com.xxxx.seckill.pojo.SeckillOrder;
import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.service.IGoodsService;
import com.xxxx.seckill.service.IOrderService;
import com.xxxx.seckill.service.ISeckillOrderService;
import com.xxxx.seckill.vo.GoodsVo;
import com.xxxx.seckill.vo.RespBeanEnum;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

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


    @RequestMapping("/doSeckill")
    public String doSeckill(Model model, User user, @Param(("goodsId")) Long goodsId){
        if(user == null){
            return "login";
        }
        GoodsVo goodsVo = goodsService.findGodsVoByGoodsId(goodsId);
        if(goodsVo.getStockCount() < 1){
            model.addAttribute("errMsg", RespBeanEnum.EMPTY_STOCK_ERROR.getMessage());
            return "seckillFail";
        }
        QueryWrapper<SeckillOrder> queryWrapper = new QueryWrapper<SeckillOrder>().eq("user_id", user.getId()).eq("goods_id", goodsId);
        SeckillOrder seckillOrder = seckillOrderService.getOne(queryWrapper);
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
