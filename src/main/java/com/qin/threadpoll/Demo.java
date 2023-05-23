package com.qin.threadpoll;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author qinSir
 * @Create 2023-05-20 12:07
 * @Description 具体描述
 */
public class Demo {
    public static void main(String[] args) {
        /**
         * 参数一： 核心线程数
         * 参数二： 最大线程数（核心线程 + 救急线程）
         * 参数三： 救急线程的存活时间
         * 参数四： 救急线程的存活时间单位
         * 参数五： 任务队列
         * 参数六： 线程工厂
         * 参数七： 拒绝策略
         */
        new ThreadPoolExecutor(5, 10, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
            private AtomicInteger atomicInteger = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r,"t" + atomicInteger.incrementAndGet());
            }
        },
                new ThreadPoolExecutor.AbortPolicy());
    }
}
