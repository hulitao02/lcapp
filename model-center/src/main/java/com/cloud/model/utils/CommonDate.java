package com.cloud.model.utils;

import java.sql.Timestamp;


/**
 *
 * @author:胡立涛
 * @description: TODO 时间计算
 * @date: 2022/4/11
 * @param:
 * @return:
 */
public class CommonDate {


    /**
     *
     * @author:胡立涛
     * @description: TODO 计算n分钟后的时间
     * @date: 2022/4/11
     * @param: [startTime 开始时间, num 经过一段时间 单位毫秒]
     * @return: java.sql.Timestamp
     */
    public static Timestamp getEndTime(Timestamp startTime,int num){
        Timestamp afterDate=new Timestamp(startTime.getTime()+num);
        return afterDate;
    }


    /**
     *
     * @author:胡立涛
     * @description: TODO 计算两个时间差值 单位：分钟
     * @date: 2022/4/11
     * @param: [start, end]
     * @return: long
     */
    public static long chaTimes(Timestamp start,Timestamp end){
        long startTime=start.getTime();
        long endTime=end.getTime();
        long timeDifference=endTime-startTime;
        long minute=timeDifference/1000/60;
        return minute;
    }


}
