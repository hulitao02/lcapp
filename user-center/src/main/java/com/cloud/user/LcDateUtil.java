package com.cloud.user;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

public class LcDateUtil {

    /**
     * 获取上周的开始时间和结束时间
     *
     * @return 上周的开始时间和结束时间
     */
    public static String getLastWeek() {
        // 获取当前日期
        LocalDate today = LocalDate.now();

        // 计算上周一（开始时间）
        LocalDate lastWeekStart = today
                .minusWeeks(1)  // 回到上周
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // 计算上周日（结束时间）
        LocalDate lastWeekEnd = today
                .minusWeeks(1)  // 回到上周
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        // 转换为带时间的 LocalDateTime（开始时间为00:00:00，结束时间为23:59:59）
        LocalDateTime startDateTime = lastWeekStart.atStartOfDay();
        LocalDateTime endDateTime = lastWeekEnd.atTime(LocalTime.MAX);

        // LocalDateTime 转 Date
        Date date = Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant());

        // 使用 SimpleDateFormat
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String startDate = sdf.format(date);
        String endDate = sdf.format(Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant()))+ " 23:59:59";

        System.out.println("上周开始时间: " + startDate);
        System.out.println("上周结束时间: " + endDate);

        return startDate + "," + endDate;
    }

    public static String getLastMonth() {
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfLastMonth = today.minusMonths(1).withDayOfMonth(1);
        LocalDate lastDayOfLastMonth = today.withDayOfMonth(1).minusDays(1);
        LocalDateTime startDateTime = firstDayOfLastMonth.atStartOfDay(); // 00:00:00
        LocalDateTime endDateTime = lastDayOfLastMonth.atTime(LocalTime.MAX); // 23:59:59.999999999
        Date date = Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant());

        // 使用 SimpleDateFormat
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String startDate = sdf.format(date);
        String endDate = sdf.format(Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant())) + " 23:59:59";

        System.out.println("上月开始时间: " + startDate);
        System.out.println("上月结束时间: " + endDate);
        return startDate + "," + endDate;
    }


    public static LocalDateTime getDateTime(String dateStr) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime date = LocalDateTime.parse(dateStr, dateTimeFormatter);
        System.out.println(date);
        return date;
    }

    public static LocalDate getDate(String dateStr) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(dateStr, dateTimeFormatter);
        System.out.println(date);
        return date;
    }


    public static void main(String[] args) {
        // getLastWeek();
        //getLastMonth();
        // 按期完成率
        // 方法1：去除百分号，转换为double计算
//        String percentStr1 = "0%";
//        String percentStr2 = "85.0%";
//
//        double percent1 = Double.parseDouble(percentStr1.replace("%", ""));
//        double percent2 = Double.parseDouble(percentStr2.replace("%", ""));
//
//        double difference = percent1 - percent2;
//
//        DecimalFormat df = new DecimalFormat("0.0");
//        System.out.println(percentStr1 + " - " + percentStr2 + " = " +
//                df.format(difference) + "%");

        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String endDate = sdf.format(Date.from(now.atZone(ZoneId.systemDefault()).toInstant())) + " 23:59:59";
        System.out.println("ddd:"+endDate);
//        String targetTime = "2026-01-15 14:30:00";
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        LocalDateTime targetTime2 = LocalDateTime.parse(targetTime, formatter);
//        boolean isAfter = now.isAfter(targetTime2);
//        if (!isAfter) {
//            System.out.println("超时");
//        }
    }
}
