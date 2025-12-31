package com.cloud.redislock;

import java.util.concurrent.TimeUnit;

/**
 * Created by dyl on 2021/07/08.
 */
public interface RedisLockInterface {

    /**
     *
     * @param key
     * @param expireTime  加锁时长
     * @return
     */
    public void lock(String key, Long expireTime, TimeUnit timeUnit);

    /**
     * @param key
     */
    public void lock(String key);
    /**
     * 解锁
     * @param key
     */
    public void unLock(String key);

    /**
     * 在指定时间内尝试,成功返回true
     * @param key
     * @param time
     * @return
     */
    public boolean tryLock(String key, Long time, TimeUnit timeUnit);

    /**
     * 在指定时间内尝试加锁,成功返回true
     * @param key
     * @param time1
     * @param time2
     * @return
     */
    public boolean tryLock(String key, Long time1, Long time2, TimeUnit timeUnit);
    /**
     * 尝试加锁，获取到返回true
     */
    public boolean tryLock(String key);

}
