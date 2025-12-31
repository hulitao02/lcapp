package com.cloud.utils;

import java.util.HashMap;

/**
 * Created by dyl on 2021/06/18.
 * 试题数量不够返回信息
 */
public class GetQuestionMessageUtils {

    public static  String getMessage(Integer questionType,Double difficultyType){
        HashMap<Integer,String> map1 = new HashMap<>();
        map1.put(1,"单选题");
        map1.put(2,"多选题");
        map1.put(3,"判断题");
        HashMap<Double,String> map2 = new HashMap<>();
        map2.put(0.2,"简单难度");
        map2.put(0.4,"一般难度");
        map2.put(0.6,"中等难度");
        map2.put(0.8,"复杂难度");
        map2.put(1.0,"困难难度");
        String msg = map2.get(difficultyType)+map1.get(questionType)+"试题数量不够";
        return  msg;
    }

}
