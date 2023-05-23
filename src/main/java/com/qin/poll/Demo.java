package com.qin.poll;

import cn.hutool.core.thread.RejectPolicy;
import cn.hutool.core.thread.ThreadFactoryBuilder;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author qinSir
 * @Create 2023-05-20 9:42
 * @Description 测试代码
 */
public class Demo {
    public static void main(String[] args) {
        MyThreadPoll threadPool = new MyThreadPoll(2,  1L,TimeUnit.SECONDS, 4,
                (blockingQueue,task) ->{
                    // 1.死等
//                    blockingQueue.put(task);
                    // 2.超时时间等
                    blockingQueue.put(task,1L,TimeUnit.SECONDS);
                    // 3.抛异常
//                    throw new RuntimeException("任务队列已满..");
                    // 4.调用者线程处理
//                    task.run();
                    // 5.调用者线程放弃任务
//                    System.out.println("放弃任务...");
                });
        for (int i = 0; i < 10; i++) {
            int j = i;
            threadPool.execute(()->{
                System.out.println(j);
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }

    }
}
