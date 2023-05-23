package com.qin.aqs;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * @Author qinSir
 * @Create 2023-05-22 11:05
 * @Description 具体描述
 */
public class Demo {
    public static void main(String[] args) {
        Lock lock = new MyLock();
        new Thread(() -> {
            lock.lock();

            try {
                System.out.println(Thread.currentThread().getName() + " 正在执行....."
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss SSS")));
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } finally {
                lock.unlock();
            }
        },"t1").start();
        new Thread(() -> {
            lock.lock();
            try {
                System.out.println(Thread.currentThread().getName() + " 正在执行....."
                        + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss SSS")));
            } finally {
                lock.unlock();
            }
        },"t2").start();
    }
}
