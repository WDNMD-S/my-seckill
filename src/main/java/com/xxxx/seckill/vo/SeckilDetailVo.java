package com.xxxx.seckill.vo;

import com.xxxx.seckill.pojo.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 秒杀详情
 *
 * @Author Azrael
 * @Date 2022/7/22 13:57
 * @Version 1.0
 * @Description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeckilDetailVo {

    private User user;

    private GoodsVo goodsVo;

    //秒杀状态
    private int seckillStatus;

    //秒杀倒计时
    private int remainSeconds;

}
