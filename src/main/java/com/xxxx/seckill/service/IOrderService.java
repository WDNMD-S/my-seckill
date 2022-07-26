package com.xxxx.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xxxx.seckill.pojo.Order;
import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.vo.GoodsVo;
import com.xxxx.seckill.vo.OrderDetailVo;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Azrael
 * @since 2022-07-18
 */
public interface IOrderService extends IService<Order> {

    /**
     * 秒杀
     * @param goods
     * @param user
     * @return
     */
    Order seckill(GoodsVo goods, User user);

    /**
     * 订单详情
     * @param orderId
     * @return
     */
    OrderDetailVo detail(Long orderId);


    /**
     * 获取秒杀链接
     * @param user
     * @param goodsId
     * @return
     */
    String getPath(User user, Long goodsId);

    /**
     * 检验秒杀路径
     * @param user
     * @param goodsId
     * @param path
     * @return
     */
    boolean checkPath(User user, Long goodsId, String path);

    /**
     * 检验验证码
     * @param user
     * @param goodsId
     * @param captcha
     * @return
     */
    boolean checkCaptcha(User user, Long goodsId, String captcha);
}
