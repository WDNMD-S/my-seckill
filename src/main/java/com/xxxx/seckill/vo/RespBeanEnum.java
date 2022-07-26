package com.xxxx.seckill.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 公共返回对象枚举
 *
 * @Author Azrael
 * @Date 2022/7/13 13:54
 * @Version 1.0
 * @Description
 */
@Getter
@AllArgsConstructor
@ToString
public enum RespBeanEnum {

    //通用
    SUCCESS(200, "SUCCESS"),
    ERROR(500, "服务端异常"),

    //登录模块
    LOGIN_ERROR(500210, "用户名或密码错误"),
    MOBILE_ERROR(500211, "手机号码格式错误"),
    BIND_ERROR(500212,"参数校验异常"),
    SESSION_ERROR(500213,"用户SESSION未找到"),

    //订单模块
    ORDER_NOT_EXIST(500300,"订单不存在"),

    //秒杀模块
    EMPTY_STOCK_ERROR(500500,"商品库存不足"),
    REPEAT_ERROR(500501,"商品限购一件"),
    REQUEST_ILLEGAL(500502,"请求非法"),
    GOODS_NOT_EXIST(500503,"商品不存在"),
    EMPTY_CAPTCHA(500504,"验证码不能为空"),
    ERROR_CAPTCHA(500505,"验证码错误"),

    ;

    private final Integer code;

    private final String message;

}
