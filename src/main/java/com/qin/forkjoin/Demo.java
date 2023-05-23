package com.qin.forkjoin;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

/**
 * @Author qinSir
 * @Create 2023-05-22 10:03
 * @Description fork-join 多线程任务拆分，合并求值
 */
public class Demo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ForkJoinPool joinPool = new ForkJoinPool();
        MyTask myTask = new MyTask(1, 5);
        joinPool.execute(myTask);
        System.out.println(myTask.get());
    }
}
