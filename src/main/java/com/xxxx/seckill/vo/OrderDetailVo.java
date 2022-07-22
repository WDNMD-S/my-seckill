package com.xxxx.seckill.vo;

import com.xxxx.seckill.pojo.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单详情
 *
 * @Author Azrael
 * @Date 2022/7/22 14:36
 * @Version 1.0
 * @Description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailVo {

    private Order order;

    private GoodsVo goodsVo;
}
