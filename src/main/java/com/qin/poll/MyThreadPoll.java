package com.qin.poll;

import cn.hutool.core.thread.RejectPolicy;

import java.util.HashSet;
import java.util.concurrent.TimeUnit;

/**
 * @Author qinSir
 * @Create 2023-05-20 9:41
 * @Description 自定义线程池
 */
public class MyThreadPoll {
    // 核心线程数
    private Integer size;
    // 线程池
    private HashSet<Worker> set;
    // 阻塞队列
    private BlockingQueue<Runnable> blockingQueue;
    // 拒绝策略
    private MyRejectPolicy<Runnable> rejectPolicy;
    // 超时时间
    private Long timeout;
    // 时间单位
    private TimeUnit timeUnit;
    public MyThreadPoll(Integer size, Long timeout, TimeUnit timeUnit, Integer blockingSize, MyRejectPolicy<Runnable> rejectPolicy) {
        this.size = size;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        set = new HashSet<>(size);
        this.rejectPolicy = rejectPolicy;
        blockingQueue = new BlockingQueue<>(blockingSize);
    }

    // 线程池执行任务
    public void execute(Runnable task){
        synchronized (this){
            // 判断线程池是否满了
            if (set.size() == size){
                // 如果满了，则加入任务队列
//                blockingQueue.put(task);
                // 使用拒绝策略解决
                blockingQueue.tryPut(rejectPolicy,task);
            }else {
                // 如果没满，则执行任务
                Worker worker = new Worker(task);
                //添加到线程池中
                set.add(worker);
                // 启动任务
                worker.start();
            }
        }
    }

    // 包装 Thread对象
    class Worker extends Thread{
        private Runnable task;
        public Worker(Runnable task){
            this.task = task;
        }
        // 执行线程任务
        @Override
        public void run() {
            // 当前线程执行完，本次任务后不会结束，而是继续执行阻塞队列中的任务，
            // 如果队列中的任务执行完了，当前线程结束, 无限时等待获取任务
//            while (task != null || (task = blockingQueue.poll()) != null){
            //如果队列中的任务执行完了，当前线程结束, 设置超时时间
            while (task != null || (task = blockingQueue.poll(timeout,timeUnit)) != null){
                try {
                    System.out.println(Thread.currentThread().getName() + ":执行任务：" + task );
                    task.run();
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    // 任务结束，移除当前task
                    System.out.println(Thread.currentThread().getName() + ":任务结束：" + task );
                    task = null;
                }
            }
            // 如果任务执行结束，则移除当前线程
            synchronized (set){
                set.remove(this);
            }
        }
    }
}
