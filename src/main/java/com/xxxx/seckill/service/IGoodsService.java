package com.xxxx.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xxxx.seckill.pojo.Goods;
import com.xxxx.seckill.vo.GoodsVo;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Azrael
 * @since 2022-07-18
 */
public interface IGoodsService extends IService<Goods> {

    /**
     * 查询所有商品信息
     * @return
     */
    List<GoodsVo> findGoodsVo();
}
