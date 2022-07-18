package com.xxxx.seckill.exception;

import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @Author Azrael
 * @Date 2022/7/14 14:28
 * @Version 1.0
 * @Description
 */
@RestControllerAdvice
public class GlobalExceptionHandle {

//    @ExceptionHandler(Exception.class)
//    public RespBean ExceptionHandle(Exception e){
//        if(e instanceof GlobalException){
//            GlobalException globalException = (GlobalException) e;
//            return RespBean.error(globalException.getRespBeanEnum());
//        }else if(e instanceof BindException){
//            BindException bindException = (BindException) e;
//            RespBean respBean = RespBean.error(RespBeanEnum.BIND_ERROR);
//            respBean.setMessage("参数校验异常: " + bindException.getBindingResult().getAllErrors().get(0).getDefaultMessage());
//            return respBean;
//        }
//        return RespBean.error(RespBeanEnum.ERROR);
//    }

}
