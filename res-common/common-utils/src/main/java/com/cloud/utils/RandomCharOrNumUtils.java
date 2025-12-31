package com.cloud.utils;

import java.util.Random;

/**
 * Created by dyl on 2021/03/28.
 */
public class RandomCharOrNumUtils {
    public static void main(String[] args) {
        System.out.println(getCharAndNum(12).toLowerCase());
        System.out.println(getChar(6));
        System.out.println(getNum(6));
    }
    /**
     * 获得随机子母和数字的组合
     * @param length
     * @return 随机字母和数字的组合
     */
    public static String getCharAndNum(int length) {
        String val = "";
        Random random = new Random();
        String charOrNum = "";
        for (int i = 0; i < length; i++) {
            if (i == 0) {
                charOrNum = "char";
            }else if (i == (length-1)) {
                charOrNum = "num";
            }else {
                charOrNum = (random.nextInt(2)%2 == 0) ? "char":"num";
            }
            if ("num".equalsIgnoreCase(charOrNum)) {//如何获得 0-9之间的数字
                int num = random.nextInt(10);
                if (num == 0) {
                    num = 2;
                }
                if (num == 1) {
                    num = 5;
                }
                val += String.valueOf(num);
            }else {//如何获得随机的字符				//97 - 122   0-25
                char char1  = (char)(97+random.nextInt(26));
                if (char1 == 'o') {
                    char1 = 'm';
                }
                val += String.valueOf(char1);
            }
        }
        val = val.toUpperCase();
        return val;

    }

    /**
     * 获得随机字母组合
     * @param length
     * @return 随机字母组合
     */
    public static String getChar(int length) {
        String val = "";
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            //如何获得随机的字符				//97 - 122   0-25
            char char1  = (char)(97+random.nextInt(26));
            val += String.valueOf(char1);
        }
        val = val.toUpperCase();
        return val;
    }

    /**
     * 获得随机数字组合
     * @param length
     * @return 随机数字组合
     */
    public static String getNum(int length) {
        String val = "";
        Random random = new Random();
        for (int i = 0; i < length; i++) {//如何获得 0-9之间的数字
            int num  = random.nextInt(10);
            val += String.valueOf(num);
        }
        val = val.toUpperCase();
        return val;
    }
}
