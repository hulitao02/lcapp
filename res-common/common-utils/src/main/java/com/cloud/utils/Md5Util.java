package com.cloud.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by pangsx
 * Date: 2018/2/8 14:49
 */
public class Md5Util {

    private final static Logger logger = LoggerFactory.getLogger(Md5Util.class);

    private Md5Util(){}

    private static class SingletonHolder{
        private static MessageDigest md ;
        static {
            try {
                md = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
    }

    public static MessageDigest getInstance(){
        return SingletonHolder.md;
    }

    /*public  static String hash(String url){
        String md5 = null;
        try {
            MessageDigest tc1 = (MessageDigest) Md5Util.getInstance().clone();
            tc1.update(url.getBytes());
            md5 = toHex(tc1.digest());
        } catch (CloneNotSupportedException e) {
            logger.error("MD5加密出现异常");
        }
        return md5;
    }*/

    public static final String toHex(byte[] hash) {
        if (hash == null) {
            return null;
        }
        StringBuffer buf = new StringBuffer(hash.length * 2);
        int i;

        for (i = 0; i < hash.length; i++) {
            if (((int) hash[i] & 0xff) < 0x10) {
                buf.append("0");
            }
            buf.append(Long.toString((int) hash[i] & 0xff, 16));
        }
        return buf.toString();
    }

    public   final   static  String hash(String s) {
        char  hexDigits[] = {  '0' ,  '1' ,  '2' ,  '3' ,  '4' ,  '5' ,  '6' ,  '7' ,  '8' ,  '9' ,
            'a' ,  'b' ,  'c' ,  'd' ,  'e' ,  'f'  };
        try  {
            byte [] strTemp = s.getBytes();
            MessageDigest mdTemp = MessageDigest.getInstance("MD5");
            mdTemp.update(strTemp);
            byte [] md = mdTemp.digest();
            int  j = md.length;
            char  str[] =  new   char [j *  2 ];
            int  k =  0 ;
            for  ( int  i =  0 ; i < j; i++) {
                byte  byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4  &  0xf ];
                str[k++] = hexDigits[byte0 & 0xf ];
            }
            return   new  String(str);
        } catch  (Exception e) {
            return   null ;
        }
    }
}
