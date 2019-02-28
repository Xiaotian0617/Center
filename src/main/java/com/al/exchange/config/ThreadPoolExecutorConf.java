package com.al.exchange.config;

import com.al.exchange.monitor.ThreadPoolMonitor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;

/**
 * 线程池的配置
 * 此处没有使用spring 的 { #ThreadPoolTaskExecutor}
 * 而是使用了jdk原生的ThreadPoolExecutor
 *
 * @author Asin Liu
 * @version 1.0
 * @since 1.0
 */
@Configuration
public class ThreadPoolExecutorConf {

    private int corePoolSize = 64;     // 核心线程数
    private int maximumPoolSize = 128;  // 最大线程数
    private int keepAliveTime = 5000;   // 保持的时间长度,单位为毫秒
    private int capacity = 10000;        // 队列的长度
    private String threadName = "Websocket";        // 线程名

    @Bean(name = "WebsocketExecutor")
    public ThreadPoolExecutor threadPoolExecutor() {


        /**
         * Creates a new {@code ThreadPoolExecutor} with the given initial
         * parameters.
         *
         * @param corePoolSize the number of threads to keep in the pool, even
         *        if they are idle, unless {@code allowCoreThreadTimeOut} is set
         * @param maximumPoolSize the maximum number of threads to allow in the
         *        pool
         * @param keepAliveTime when the number of threads is greater than
         *        the core, this is the maximum time that excess idle threads
         *        will wait for new tasks before terminating.
         * @param unit the time unit for the {@code keepAliveTime} argument
         * @param workQueue the queue to use for holding tasks before they are
         *        executed.  This queue will hold only the {@code Runnable}
         *        tasks submitted by the {@code execute} method.
         * @param threadFactory the factory to use when the executor
         *        creates a new thread
         * @param handler the handler to use when execution is blocked
         *        because the thread bounds and queue capacities are reached
         * @throws IllegalArgumentException if one of the following holds:<br>
         *         {@code corePoolSize < 0}<br>
         *         {@code keepAliveTime < 0}<br>
         *         {@code maximumPoolSize <= 0}<br>
         *         {@code maximumPoolSize < corePoolSize}
         * @throws NullPointerException if {@code workQueue}
         *         or {@code threadFactory} or {@code handler} is null
         */
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
                keepAliveTime, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(capacity), new CallerRunsPolicy());
        ThreadPoolMonitor monitor = new ThreadPoolMonitor(threadPoolExecutor, 3, 1, threadName);
        Thread monitorThread = new Thread(monitor);
        monitorThread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            threadPoolExecutor.shutdown();
            monitor.stop();
        }));
        return threadPoolExecutor;
    }
}