package com.qin.output;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author qinSir
 * @Create 2023-05-17 16:19
 * @Description 多线程交替打印
 *  顺序打印 A  ->  B  ->  C,总共打印5次
 */
public class AlternatingOutput {
    private static Lock lock = new ReentrantLock();
    private static Condition conditionA = lock.newCondition();
    private static Condition conditionB = lock.newCondition();
    private static Condition conditionC = lock.newCondition();
    public static void main(String[] args) throws InterruptedException {
        new Thread(() -> {
            printStr("A",5,conditionA,conditionB);
        },"T1").start();
        new Thread(() -> {
            printStr("B",5,conditionB,conditionC);
        },"T2").start();
        new Thread(() -> {
            printStr("C",5,conditionC,conditionA);
        },"T3").start();

        TimeUnit.SECONDS.sleep(1);
        lock.lock();
        try {
            conditionA.signal();
        }finally {
            lock.unlock();
        }

    }

    public static void printStr(String str,Integer num,Condition current,Condition next){
        for (int i = 0; i < num; i++) {
            lock.lock();
            try {
                current.await();
                System.out.println(Thread.currentThread().getName() + ":" + str);
                next.signal();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
        }
    }
}
