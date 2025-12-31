/**
 * Copyright 2002-2010 the original author or authors.
 */
package com.cloud.utils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 验证字符类
 *
 * @author Zhang Jianhua
 */
public class Validator {

    private static final String validChina = "[\\u4E00-\\u9FA5]";
    private static final String validNotYMD_cn = "^20\\d{2}年\\d{1,2}月\\d{1,2}日.*";
    private static final String validNumber = "^[0-9]*$";
    private static final String validNewOne = "^[\\u4e00-\\u9fa5A-Za-z\\d_\\-\\/—\\(\\)、（）\\s]+$";//中文、英文、数字、符号 —_ / /^[\u4e00-\u9fa5A-Za-z\d_\-\/—\(\)、（）\s]+$/
    private static final String validNewTwo = "^[A-Za-z\\d_\\-\\/\\s]+$";//英文、数字、符号 —_ /
    private static final String validNum = "^20\\d{10}";
    private static final String validEnglish="[a-zA-Z ]+";
    private static final String validChinaese="[\u4e00-\u9fa5]+";
    //验证 00:00:00结尾
    private static final String validYMD_Zero = "^.*00:00:00$";
    private static Pattern p;
    private static Matcher m;


    /**
     * 验证 20xx年xx/x月xx/x日 中文日期格式
     *
     * @param str 传入的字符串
     * @return 如果为中文日期格式则返回true，否则返回false
     */
    public static boolean validNotYMD_cn(String str) {
        boolean flag = isEqualString(str, validNotYMD_cn);
        return flag;
    }

    /**
     * 判断两个字符串是否相等
     *
     * @param str 传入的字符串
     * @return 如果两个字符串相等则为true，否则为false
     * //此方法持续占用过高CPU by weiminggui date 2014-1-22
     */
    public static boolean isEqualString(String str, String val) {
        boolean flag = false;
        try {
            p = Pattern.compile(val);// 设置比较模式
            m = p.matcher(str);
            flag = m.matches();
        } catch (Exception e) {
        }
        return flag;
    }


    /**
     * 中文匹配
     *
     * @param str 传入的字符串
     * @return 如果str是中文则为true，否则为false
     */
    public static boolean isValidChinese(String str) {
        Pattern p = Pattern.compile(validChina);
        Matcher m = p.matcher(str);
        return m.find();
    }

    /**
     * 只能输入中文
     * @param str
     * @return
     */
    public static boolean isChineseWord(String str){
        boolean isMatch =  Pattern.matches(validChinaese, str);
        return isMatch;
    }


    /**
     * 只能输入英文和空格
     * @param str
     * @return
     */
    public static boolean isEnglishWord(String str){
        boolean isMatch =  Pattern.matches(validEnglish, str);
        return isMatch;
    }



    /**
     * 只能输入数字
     *
     * @param str 传入的字符串
     * @return 如果str为数字则为true，否则为false
     */
    public static boolean isValidNumber(String str) {
        boolean flag = isEqualString(str, validNumber);
        return flag;
    }

    /**
     * 判断对象数组是否为空
     *
     * @param object 对象数组
     * @return 如果object为空返回true，不为空返回false
     */
    public static boolean isNull(Object[] object) {
        return null == object || object.length == 0;
    }

    /**
     * 判断一个字符串是否为空
     *
     * @param str 传入的字符串
     * @return 如果str为空返回true，不为空返回false
     */
    public static boolean isNull(String str) {
        return null == str || str.trim().isEmpty() || str.trim().equals("null");
    }

    /**
     * 判断字符串不能为空
     *
     * @param str 传入的字符串
     * @return 如果str不为空返回true，为空返回false
     */
    public static boolean isNotNull(String str) {
        return !isNull(str);
    }

    /**
     * 判断对象是否为空
     *
     * @param obj 传入的对象
     * @return 如果obj为空返回true，不为空返回false
     */
    public static boolean isEmpty(Object obj) {
        return obj == null;
    }

    /**
     * 判断一个list集合是否为空
     *
     * @param list 传入的list
     * @return 如果list为空或者长度为0返回true，不为空返回false
     */
    public static boolean isEmpty(List list) { //
        return list == null || list.size() == 0;
    }

    /**
     * 判断一个Map集合是否为空
     *
     * @param map 传入的map
     * @return 如果map为空或者长度为0返回true，不为空返回false
     */
    public static boolean isEmpty(Map map) { //
        return map == null || map.size() == 0;
    }

    /**
     * 判断一个list集合是否为空
     *
     * @param collection 传入的集合
     * @return 如果collection为空或者长度为0返回true，不为空返回false
     */
    public static boolean isEmpty(Collection collection) { //
        return collection == null || collection.isEmpty();
    }

    /**
     * 判断是否含有特殊字符
     *
     * @param str
     * @return true为包含，false为不包含
     */
    public static boolean isSpecialChar(String str) {
        String regEx = "[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\n|\r|\t";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.find();
    }

    /**
     * 中文、英文、数字、符号 —_ /
     *
     * @param str 传入的字符串
     * @return 如果str是英文玉数字组合则为true，否则为false
     */
    public static boolean isValidNewOne(String str) {
        boolean flag = isEqualString(str, validNewOne);
        return flag;
    }

    /**
     * 英文、数字、符号 —_ /
     *
     * @param str 传入的字符串
     * @return 如果str是英文玉数字组合则为true，否则为false
     */
    public static boolean isValidNewTwo(String str) {
        boolean flag = isEqualString(str, validNewTwo);
        return flag;
    }

    public static void main(String[] args) {
     //   System.out.println(validNum("20200127070500"));
        //		boolean b = isValidDate("20170228");
        //		boolean b = isValidDate("2020-02-12 22:58:48");
        //		boolean b = isValidDate("2020-02-12 22:58:48");
        //		boolean b = validYMDZero("2020-02-12 22:58:48");
        //		boolean b = validNum("20200127070100");
        //		boolean b = isValidNewOne("诉求类型");
//        		boolean b1 = isChineseWord("孔德w强");
//                 boolean b1 = isEnglishWord("wdds vv1dfytj");
//        System.out.println(b1);
        //		boolean bb = validNotYMD_cn("2020年2月1日");
       // boolean bb = validYMDZero("www00:00:00");
        		//System.out.println(b);
       // System.out.println(bb);


    }

    /**
     * 验证 00:00:00结尾
     *
     * @return 验证 00:00:00结尾 true 是 false 否
     */
//    public static boolean validYMDZero(String str) {
//        return isEqualString(str, validYMD_Zero);
//    }

    /**
     * 验证数字为12位
     *
     * @param
     * @return true
     */
    public static boolean validNum(String str) {
        return isEqualString(str, validNum);
    }
}
