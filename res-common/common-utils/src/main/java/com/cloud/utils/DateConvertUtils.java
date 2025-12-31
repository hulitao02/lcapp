package com.cloud.utils;

import cn.hutool.core.util.ObjectUtil;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by dyl on 2021/03/26.
 */
public class DateConvertUtils {
    private static ThreadLocal<SimpleDateFormat> ThreadDateTime = new ThreadLocal<SimpleDateFormat>();
    private static String TimestampFormat = "yyyy-MM-dd HH:mm:ss";


    private static SimpleDateFormat TimestampInstance() {
        SimpleDateFormat df = ThreadDateTime.get();
        if (df == null) {
            df = new SimpleDateFormat(TimestampFormat);
            ThreadDateTime.set(df);
        }
        return df;
    }

    public static Date StringTime2Data(String time){
        Date data = null;
        try {
            data = TimestampInstance().parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return data;
    }
    public static String timeStamp2Str(Timestamp time) {

        return TimestampInstance().format(time);
    }

    public static String timeStamp2Str(Date time) {

        return TimestampInstance().format(time);
    }
    public static String data2Format(){

        return  TimestampInstance().format(new Date());
    }

    public static String date2Str(Date time) {

        return TimestampInstance().format(time);
    }

    //转换函数，可以封装成公用方法
    public static String longTimeToDay(Date startTime,Date endTime){
        Long ms =  endTime.getTime() - startTime.getTime();
        Integer ss = 1000;
        Integer mi = ss * 60;
        Integer hh = mi * 60;
        Integer dd = hh * 24;

        Long day = ms / dd;
        Long hour = (ms - day * dd) / hh;
        Long minute = (ms - day * dd - hour * hh) / mi;
        Long second = (ms - day * dd - hour * hh - minute * mi) / ss;
        Long milliSecond = ms - day * dd - hour * hh - minute * mi - second * ss;

        StringBuffer sb = new StringBuffer();
        if(day > 0) {
            sb.append(day+"天");
        }
        if(hour > 0) {
            sb.append(hour+"小时");
        }
        if(minute > 0) {
            sb.append(minute+"分");
        }
        if(second > 0) {
            sb.append(second+"秒");
        }
        /*if(milliSecond > 0) {
            sb.append(milliSecond+"毫秒");
        }*/
        return sb.toString();
    }

    public static Long getExtTime(Date startTime,Date endTime){
        return endTime.getTime()-startTime.getTime();
    }

    public static String assembleTime(Date startTime,Date endTime){
        String format1 = TimestampInstance().format(startTime);
        String format2 = TimestampInstance().format(endTime);
        return format1+"~"+format2;
    }

    public static String getPattenTime(Date startTime,Date endTime){

        String format1 = TimestampInstance().format(startTime);//2021-12-07 10:28:00
        String format2 = TimestampInstance().format(endTime);//2022-01-31 10:28:00
        /*String[] split = format1.split("-");
        String s = split[2];
        String[] split1 = s.split(" ");
        String[] split2 = split1[1].split(":");

        String[] splits = format2.split("-");
        String ss = splits[2];
        String[] splits1 = ss.split(" ");
        String[] splits2 = splits1[1].split(":");

        String str = split[0]+"年"+split[1]+"月"+split1[0]+"日 ";
        if(Integer.valueOf(split2[0])<12){
            str +="上午";
        }else{
            str +="下午";
        }
        str+=split2[0]+":"+split2[1]+"至"+splits2[0]+":"+splits2[1];*/
        format1 = format1.replaceAll("-","/");
        format2 = format2.replaceAll("-","/");

        return format1+" - "+format2;


    }

    public static void main(String[] args) throws ParseException {
        String format1 = "2021-12-07 10:28:00";
        String format2 = "2021-12-08 10:28:00";
        format1 = format1.replaceAll("-","/");
        format2 = format2.replaceAll("-","/");
        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");
        System.out.println(format1+" - "+format2);
        /*List<Date> week = getWeek("2022-01-19");
           week.stream().forEach(e->{
           System.out.println(sd.format(e));
        });*/
        SimpleDateFormat sd1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date parse  = sd1.parse("2022-01-03 15:15:00");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parse);
        int i = calendar.get(Calendar.HOUR_OF_DAY);
        System.out.println(i);
    }

    public static int getDayOfWeek(Date date){
        Calendar cal = Calendar.getInstance();
        if(ObjectUtil.isEmpty(date)){
            cal.setTime(new Date(System.currentTimeMillis()));
        }else {
            SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //Date parse  = sd.parse(date);
            cal.setTime(new Date(date.getTime()));

        }
        return cal.get(Calendar.DAY_OF_WEEK)-1;
    }

    public static List<Date> getWeek(String date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        int b = 0;
        Date date1 = new Date();
        if(ObjectUtil.isEmpty(date)){
            b = new Date().getDay();
        }else {
            try {
                date1 = simpleDateFormat.parse(date);
                b = date1.getDay();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        Date d ;
        List<Date> ll = new ArrayList<>();
        Long time = date1.getTime()-b*24*3600000;
        for (int a = 1;a<9;a++){
            d = new Date();
            d.setTime(time+(a*24*3600000));
            ll.add(a-1,d);
        }
        return ll;
    }

    public static String getHourAndMin(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return  cal.get(Calendar.HOUR_OF_DAY)+":"+cal.get(Calendar.MINUTE);
    }
    public static Long getMinOfTime(Date startTime,Date endTime){

        return  (endTime.getTime()-startTime.getTime())/(1000*60);
    }

    public static String getFirstDayOfMonth(boolean flag){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        GregorianCalendar gregorianCalendar = (GregorianCalendar)Calendar.getInstance();
        gregorianCalendar.setTime(date);
        //gregorianCalendar.add(Calendar.MONTH,0);
        gregorianCalendar.set(Calendar.DAY_OF_MONTH,1);
        String firstDay = sdf.format(gregorianCalendar.getTime());
        StringBuffer stringBuffer = new StringBuffer().append(firstDay);
        if(flag){
            stringBuffer.append(" 00:00:00");
        }
        return stringBuffer.toString();
    }
}
