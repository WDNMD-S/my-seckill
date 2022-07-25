package com.xxxx.seckill.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author Azrael
 * @Date 2022/7/24 15:10
 * @Version 1.0
 * @Description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeckillMessage {

    private User user;
    private Long goodsId;

}
