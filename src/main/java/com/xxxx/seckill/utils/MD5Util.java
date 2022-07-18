package com.xxxx.seckill.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

/**
 * MD5工具类
 *
 * @Author Azrael
 * @Date 2022/7/12 18:11
 * @Version 1.0
 * @Description
 */
@Component
public class MD5Util {

    //指定第一次加密使用的盐
    public static final String salt = "1a2b3c4d";

    public static String md5(String src) {
        return DigestUtils.md5Hex(src);
    }

    /**
     * 第一次加密，从网页到服务器,使用固定盐
     *
     * @param inputPass
     * @return
     */
    public static String inputPassToFormPass(String inputPass) {
        String str = new StringBuffer(inputPass)
                .insert(0, salt.charAt(3))
                .insert(0, salt.charAt(1))
                .append(salt.charAt(4))
                .append(salt.charAt(2))
                .toString();
        return md5(str);
    }

    /**
     * 第二次加密，从服务器到数据库，使用参数中的盐
     *
     * @param formPass
     * @param salt
     * @return
     */
    public static String formPassToDBPass(String formPass, String salt) {
        String str = new StringBuffer(formPass)
                .insert(0, salt.charAt(3))
                .insert(0, salt.charAt(1))
                .append(salt.charAt(4))
                .append(salt.charAt(2))
                .toString();
        return md5(str);
    }

    /**
     * 最终调用方法
     *
     * @param inputPass
     * @param salt
     * @return
     */
    public static String inputPassToDBPass(String inputPass, String salt) {
        String formPass = inputPassToFormPass(inputPass);
        String dbPass = formPassToDBPass(formPass, salt);
        return dbPass;
    }

    public static void main(String[] args) {
        //测试
        String formPass = inputPassToFormPass("qwer123");//19b3ab33d5e8960bdb239229bb6f9f4e
        System.out.println(formPass);
        String dbPass = formPassToDBPass(formPass, "1a2b3c4d");
        System.out.println(dbPass);
        System.out.println(inputPassToDBPass("qwer123", "1a2b3c4d"));
    }

}
