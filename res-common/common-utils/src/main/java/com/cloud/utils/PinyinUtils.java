package com.cloud.utils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;

/**
 * Created by dyl on 2021/09/06.
 */
public class PinyinUtils {

    public static String getAllPinyin(String hanzi){
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_U_UNICODE);
        char[] hanyuArr = hanzi.trim().toCharArray();
        StringBuilder  stringBuilder = new StringBuilder();
        try {
            for(int i = 0,len = hanyuArr.length;i<len;i++){
                if(Character.toString(hanyuArr[i]).matches("[\\u4E00-\\u9FA5]+")){
                    String[]  pys = PinyinHelper.toHanyuPinyinStringArray(hanyuArr[i],format);
                    stringBuilder.append(pys[0]).append("");
                }else {
                    stringBuilder.append(hanyuArr[i]).append("");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public static void main(String[] args) {
        String te = getAllPinyin("鼎折覆餗");
        System.out.println(te);
    }
}
