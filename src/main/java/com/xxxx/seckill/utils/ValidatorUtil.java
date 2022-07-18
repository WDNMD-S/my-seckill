package com.xxxx.seckill.utils;

import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author Azrael
 * @Date 2022/7/13 19:43
 * @Version 1.0
 * @Description
 */
public class ValidatorUtil {

    private static final Pattern MOBILE_PATTERN = Pattern.compile("^1((3[0-9])|(4[5-9])|(5([0-35-9]))|(6[567])|(7[0-8])|(8[0-9])|(9[0-35-9]))\\d{8}$");

    public static boolean isMobile(String mobile) {
        if (StringUtils.isEmpty(mobile)) {
            return false;
        }
        Matcher matcher = MOBILE_PATTERN.matcher(mobile);
        return matcher.matches();
    }

}
