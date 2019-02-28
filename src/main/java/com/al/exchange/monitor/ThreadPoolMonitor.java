package com.al.exchange.monitor;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
public final class ThreadPoolMonitor implements Runnable {
    private ThreadPoolExecutor executor;
    private int delay;
    private int tps;
    private String name;
    //是否要持续监控
    private volatile boolean runFlag = true;

    public ThreadPoolMonitor(ThreadPoolExecutor executor) {
        this(executor, 1, 1);
    }

    public ThreadPoolMonitor(ThreadPoolExecutor executor, int delay) {
        this(executor, delay, 1);
    }

    /**
     * 构造
     *
     * @param executor 要监控的线程池
     * @param delay    启动时延时时间
     * @param tps      多长时间采样一次，以秒为单位
     */
    public ThreadPoolMonitor(ThreadPoolExecutor executor, int delay, int tps) {
        this.executor = executor;
        this.delay = delay * 1000;
        this.tps = tps * 1000;
    }

    /**
     * 构造
     *
     * @param executor 要监控的线程池
     * @param delay    启动时延时时间
     * @param tps      多长时间采样一次，以秒为单位
     * @param name     线程名称，用于日志中标识此线程是谁创建的
     */
    public ThreadPoolMonitor(ThreadPoolExecutor executor, int delay, int tps, String name) {
        this.executor = executor;
        this.delay = delay * 1000;
        this.tps = tps * 1000;
        this.name = name;
    }


    /**
     * 停止监控ThreadPoolExecutor
     */
    public void stop() {
        runFlag = false;
    }


    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    @SneakyThrows
    public void run() {
        Thread.sleep(delay);
        while (runFlag) {
            log.info("[thread A/C]:[{}/{}] Active: {}, Completed: {}, Task: {}, isShutdown: {}, isTerminated: {}, threadCreator: {}",
                    executor.getPoolSize(),
                    executor.getCorePoolSize(),
                    executor.getActiveCount(),
                    executor.getCompletedTaskCount(),
                    executor.getTaskCount(),
                    executor.isShutdown(),
                    executor.isTerminated(),
                    name
            );
            Thread.sleep(tps);
        }
    }
}
