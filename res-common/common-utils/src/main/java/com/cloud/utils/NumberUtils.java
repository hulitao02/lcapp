package com.cloud.utils;

import java.text.DecimalFormat;

/**
 * Created by dyl on 2021/09/29.
 */
public class NumberUtils {

    public static Double toFloat(Double targetNum,Integer denominator){
        DecimalFormat df = new DecimalFormat("0.00");
        return Double.valueOf(df.format(targetNum/denominator));
    }

    public static Double toDouble(Double targetNum,Integer denominator){

        return targetNum*denominator;
    }

    public static void main(String[] args) {
        System.out.println(toFloat(52.0,100));
    }
}
