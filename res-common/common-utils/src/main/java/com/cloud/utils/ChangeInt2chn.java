package com.cloud.utils;

/**
 * Created by dyl on 2021/07/05.
 */
public class ChangeInt2chn {

    static StringBuffer sb;
    public static void main(String[] args) {
        System.out.println(tranInt(11));
    }

    public static String tranInt(int num){
        sb = new StringBuffer();
        if(num>=20 || num < 10){
            int q = (num%10000);
            changeGe(q);
        }
        if(num>=10 && num <20){
            littleThan20(num);
        }
        if(num!=0){
            if(sb.length()>=0 && '零'==(sb.charAt(0))){
                sb.deleteCharAt(0);
            }
        }
        if(num==0){
            sb.append('零');
        }
        return sb.toString();
    }

    public static String transfer(Integer num){
        String str = "零";
        switch (num){
            case 0:
                str="零";
                break;
            case 1:
                str="一";
                break;
            case 2:
                str="二";
                break;
            case 3:
                str="三";
                break;
            case 4:
                str="四";
                break;
            case 5:
                str="五";
                break;
            case 6:
                str="六";
                break;
            case 7:
                str="七";
                break;
            case 8:
                str="八";
                break;
            case 9:
                str="九";
                break;
            case 10:
                str="十";
                break;
            default:
                break;
        }

        return str;
    }
    public static String changeName(int num){
        String str = null;
        switch (num){
            case 0:
                str="";
                break;
            case 1:
                str="十";
                break;
            case 2:
                str="百";
                break;
            case 3:
                str="千";
                break;
            default:
                break ;
        }

        return str;
    }

    public static void changeGe(int t){
        StringBuffer stringBuffer = new StringBuffer();
        for(int i = 0;i<4;i++){
            int ib = t%10;
            t = t/10;
            if(ib!=0){
                stringBuffer.append(changeName(i));
                stringBuffer.append(transfer(ib));
            }else if(stringBuffer.length()>0 && '零'==(stringBuffer.charAt(stringBuffer.length()-1))){

            }else{
                stringBuffer.append("零");
            }
        }
        char c = stringBuffer.charAt(0);
        if('零'==(stringBuffer.charAt(0))){
            stringBuffer.deleteCharAt(0);
        }
        sb.append(stringBuffer.reverse());
    }
    public static void littleThan20(int num){
        int i = num%10;
        sb.append("十");
        sb.append(transfer(i));
    }
}
