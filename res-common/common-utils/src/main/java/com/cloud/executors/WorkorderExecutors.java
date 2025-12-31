package com.cloud.executors;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @date: 2021/2/2413:46
 */
public class WorkorderExecutors {

    private final int arrayBlockingQueueSize = 10000;

    /*单理模式*/
    private volatile static WorkorderExecutors workorderExecutors;

    private WorkorderExecutors() {
    }

    /***
     * @Description:  --> 双重检查锁
     * @Param: []
     * @return: cn.net.yzl.workorder.executors.WorkorderExecutors
     * @Author: 11616
     * @Date: 2021/4/27
     */
    public static WorkorderExecutors getInstance() {
        if (null == workorderExecutors) {
            synchronized (WorkorderExecutors.class) {
                if (null == workorderExecutors) {
                    workorderExecutors = new WorkorderExecutors();
                }
            }
        }
        return workorderExecutors;
    }

    /**
     * 线程池 队列，定长队列 1000
     */
    private final ArrayBlockingQueue arrayBlockingQueue = new ArrayBlockingQueue(arrayBlockingQueueSize);

    /**
     * 自定义线程池
     */
    public final ExecutorService customerService = new ThreadPoolExecutor(
            2,
            3,
            0L,
            TimeUnit.SECONDS,
            arrayBlockingQueue,
            Executors.defaultThreadFactory(),
            new CustomRejectedExecutionHandler());


    /**
     * 自定义线程池
     */
    public final ExecutorService customerSingleService = Executors.newSingleThreadExecutor();


    /**
     * 关闭线程池
     *
     * @param customerService
     */
    public void closePool(ExecutorService customerService) {
        try {
            customerService.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 自定义 拒绝策略
     */
    private class CustomRejectedExecutionHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            try {
                executor.getQueue().put(r);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
