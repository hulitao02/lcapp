package com.cloud.exam.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;

public class Tools {

    private static Cache<String, String> kpCache = CacheBuilder.newBuilder().maximumSize(200).expireAfterWrite(24, TimeUnit.HOURS).build();

    private static Cache<Long, String> userCache = CacheBuilder.newBuilder().maximumSize(200).expireAfterWrite(4, TimeUnit.HOURS).build();

    private static Cache<String, Object> deptCache = CacheBuilder.newBuilder().maximumSize(200).expireAfterWrite(4, TimeUnit.HOURS).build();

    private static Cache<Object, Object> judgeCache = CacheBuilder.newBuilder().maximumSize(200).expireAfterWrite(1, TimeUnit.DAYS).build();

    private static Cache<String, Object> requestCache = CacheBuilder.newBuilder().maximumSize(200).expireAfterWrite(3, TimeUnit.SECONDS).build();

    //竞答考试中每组分数key: 活动id&座位号 value：分数
    private static Cache<String, Double> competitionScoreCache = CacheBuilder.newBuilder().maximumSize(200).expireAfterWrite(1, TimeUnit.DAYS).build();
    //竞答考试中每组名称key: 活动id&座位号 value：名称
    private static Cache<String, String> competitionGroupCache = CacheBuilder.newBuilder().maximumSize(200).expireAfterWrite(1, TimeUnit.DAYS).build();

    public static String getCompetitionGroupCache(String key) {
        return competitionGroupCache.getIfPresent(key);
    }

    public static void putCompetitionGroupCache(String key, String val) {
        competitionGroupCache.put(key, val);
    }

    public static Double getCompetitionScoreCache(String key) {
        return competitionScoreCache.getIfPresent(key);
    }

    public static void putCompetitionScoreCache(String key, Double val) {
        competitionScoreCache.put(key, val);
    }

    public static String getKpCacheValue(String key) {
        return kpCache.getIfPresent(key);
    }

    public static void putKpCacheValue(String key, String val) {
        kpCache.put(key, val);
    }

    public static String getUserCacheValue(Long key) {
        return userCache.getIfPresent(key);
    }

    public static void putUserCacheValue(Long key, String val) {
        userCache.put(key, val);
    }

    public static String getDeptCacheValue(Long key) {
        return userCache.getIfPresent(key);
    }

    public static void putDeptCacheValue(Long key, String val) {
        userCache.put(key, val);
    }

    public static Object getJudgeCache(Object key) {
        return judgeCache.getIfPresent(key);
    }

    public static void putJudgeCache(Object key, Object value) {
        judgeCache.put(key, value);
    }
    public static Object getDeptCache(String key) {
        return deptCache.getIfPresent(key);
    }

    public static void putDeptCache(String key, Object value) {
        deptCache.put(key, value);
    }
    public static Object getRequestCache(String key) {
        return requestCache.getIfPresent(key);
    }

    public static void putRequestCache(String key, Object value) {
        requestCache.put(key, value);
    }

    public static void removeJudgeCache() {
        judgeCache.invalidateAll();
    }

}
