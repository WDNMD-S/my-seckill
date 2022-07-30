package com.xxxx.seckill.controller;

import com.google.gson.Gson;
import com.wf.captcha.ArithmeticCaptcha;
import com.xxxx.seckill.exception.GlobalException;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author Azrael
 * @Date 2022/7/19 19:36
 * @Version 1.0
 * @Description
 */
@Slf4j
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


    private Map<Long, Boolean> emptyStock = new HashMap<>();


    @RequestMapping("/getPath")
    @ResponseBody
    public RespBean getPath(User user, @Param("goodsId") Long goodsId,@Param("captcha")String captcha) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        if( goodsId < 0){
            return RespBean.error(RespBeanEnum.GOODS_NOT_EXIST);
        }
        if(StringUtils.isEmpty(captcha)){
            return RespBean.error(RespBeanEnum.EMPTY_CAPTCHA);
        }
        boolean check = orderService.checkCaptcha(user,goodsId,captcha);
        if(!check){
            return RespBean.error(RespBeanEnum.ERROR_CAPTCHA);
        }
        String path = orderService.getPath(user, goodsId);
        return RespBean.success(path);
    }

    @GetMapping("/captcha")
    public void getCaptcha(User user, @Param("goodsId") Long goodsId, HttpServletResponse response) {
        if (user == null) {
            throw new GlobalException(RespBeanEnum.SESSION_ERROR);
        }
        if( goodsId < 0){
            throw new GlobalException(RespBeanEnum.GOODS_NOT_EXIST);
        }
        response.setContentType("image/jpg");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(130, 32, 3);
        redisTemplate.opsForValue().set("captcha:" + user.getId() + ":" + goodsId, captcha.text(), 300, TimeUnit.SECONDS);
        try {
            captcha.out(response.getOutputStream());
        } catch (IOException e) {
            log.error("验证码生成失败", e.getMessage());
        }
    }

    /**
     * 秒杀(页面静态化)
     *
     * @param path
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/{path}/doSeckill", method = RequestMethod.POST)
    @ResponseBody
    public RespBean doSeckill(@PathVariable("path") String path, User user, @Param("goodsId") Long goodsId) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        if( goodsId < 0){
            return RespBean.error(RespBeanEnum.GOODS_NOT_EXIST);
        }
        //检查自定义路径
        boolean check = orderService.checkPath(user, goodsId, path);
        if (!check) {
            return RespBean.error(RespBeanEnum.REQUEST_ILLEGAL);
        }

        ValueOperations valueOperations = redisTemplate.opsForValue();
        //判断是否重复购买
        SeckillOrder seckillOrder = (SeckillOrder) valueOperations.get("order:" + user.getId() + ":" + goodsId);
        if (seckillOrder != null) {
            return RespBean.error(RespBeanEnum.REPEAT_ERROR);
        }
        //使用内存标记减少Redis访问次数
        if (emptyStock.get(goodsId)) {
            return RespBean.error(RespBeanEnum.EMPTY_STOCK_ERROR);
        }
        Long stockCount = valueOperations.decrement("seckillGoods:" + goodsId);
        if (stockCount < 0) {
            emptyStock.put(goodsId, true);
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


    @RequestMapping(value = "/getResult", method = RequestMethod.GET)
    @ResponseBody
    public RespBean getResult(User user, @Param("goodsId") Long goodsId) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        if( goodsId < 0){
            return RespBean.error(RespBeanEnum.GOODS_NOT_EXIST);
        }
        return RespBean.success(seckillOrderService.getResult(user, goodsId));
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
        goodsVo.forEach(goodsVo1 -> {
            //将商品库存缓存到Redis中
            redisTemplate.opsForValue().set("seckillGoods:" + goodsVo1.getId(), goodsVo1.getStockCount());
            emptyStock.put(goodsVo1.getId(), false);
        });
    }
}
