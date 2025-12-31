package com.cloud.redislock;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Created by dyl on 2021-6-10.
 * redis setnx
 */
@Component
public class RedisLock implements RedisLockInterface {

    private static final String lock_key="lock_key";
    @Autowired
    private RedissonClient redissonClient;

    /**
     * 解锁
     * @param key
     */
    @Override
    public void unLock(String key){
        RLock lock = redissonClient.getLock(key);
        lock.unlock();
    }

    /**
     * @param key
     */
    @Override
    public void lock(String key){
        RLock lock = redissonClient.getLock(key);
        lock.lock();
    }
    /**
     * @param key
     * @param expireTime  加锁时长
     * @return
     */
    @Override
    public void lock(String key, Long expireTime,TimeUnit timeUnit){
        RLock lock = redissonClient.getLock(key);
        lock.lock(expireTime, timeUnit);
    }

    /**
     * 在指定时间内尝试加锁
     * @param key
     * @param time
     * @return
     */
    @Override
    public boolean tryLock(String key,Long time,TimeUnit timeUnit){
        RLock lock = redissonClient.getLock(key);
        boolean b = false;
        try {
            //尝试加锁，最多等待time秒
            b = lock.tryLock(time, timeUnit);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return b;
    }

    /**
     * 在指定时间内尝试加锁
     * @param key
     * @param time1
     * @param time2
     * @return
     */
    @Override
    public boolean tryLock(String key,Long time1,Long time2,TimeUnit timeUnit){
        RLock lock = redissonClient.getLock(key);
        boolean b = false;
        try {
            //尝试加锁，最多等待time1 ,上锁以后 time2 后自动释放锁
            b = lock.tryLock(time1,time2, timeUnit);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return b;
    }
    @Override
    public boolean tryLock(String key) {
        RLock lock = redissonClient.getLock(key);
        boolean  b = false;
        try {
            b = lock.tryLock(1,15000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return b;
    }
}
