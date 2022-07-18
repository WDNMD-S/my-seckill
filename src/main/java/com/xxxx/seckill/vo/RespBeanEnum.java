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
    BIND_ERROR(500212,"参数校验异常");

    private final Integer code;

    private final String message;

}