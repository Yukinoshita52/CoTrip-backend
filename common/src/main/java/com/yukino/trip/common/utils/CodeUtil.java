package com.yukino.trip.common.utils;

import java.util.Random;

/**
 * ClassName: CodeUtil
 * Package: com.yukino.lease.common.utils
 * Description:
 *
 * @Author YukinoshitaYukino
 * @Create 2025/3/28 14:13
 * @Version 1.0
 */
public class CodeUtil {

    public static String getRandomCode(Integer length){
        StringBuilder stringBuilder = new StringBuilder(length);
        Random random = new Random();
        for(int i=0;i<length;i++){
            stringBuilder.append(random.nextInt(10));
        }
        return stringBuilder.toString();
    }
}
