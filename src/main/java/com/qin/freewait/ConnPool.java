package com.qin.freewait;

import java.sql.Connection;
import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * @Author qinSir
 * @Create 2023-05-19 15:22
 * @Description 连接池模拟， 享元模式
 */
public class ConnPool {
    // 连接池大小
    private Integer size;
    // 连接池
    private Connection[] connections;
    // 连接池状态 0表示可用，1表示不可用
    private AtomicIntegerArray atomicIntegerArray;
    public ConnPool(int size){
        this.size = size;
        connections = new Connection[size];
        atomicIntegerArray = new AtomicIntegerArray(new int[size]);
        for (int i = 0; i < size; i++) {
            connections[i] = new MyConn("连接" + i);
        }
    }

    // 获取连接
    public Connection getConn(){
        while (true){
            for (int i = 0; i < size; i++) {
                // 如果连接状态为0 ，表示可用,修改状态成功，则进行返回当前连接
                if (atomicIntegerArray.get(i) == 0 && atomicIntegerArray.compareAndSet(i,0,1)){
                    System.out.println("获取到连接：" + Thread.currentThread().getName() + " " + connections[i]);
                    return connections[i];
                }
            }
            // 如果没有找到连接则进行等待
            synchronized (this){
                try {
                    System.out.println("等待获取连接：" + Thread.currentThread().getName() );
                    wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    // 关闭连接
    public void close(Connection conn){
        for (int i = 0; i < size; i++) {
            // 如果是当前连接，则进行关闭
            if (connections[i] == conn){
                // 状态改为0 ， 因为conn只能获取一次，不存在多个线程持有同一个conn,所以关闭时，无需使用CAS
                atomicIntegerArray.set(i,0);
                System.out.println("关闭连接：" + Thread.currentThread().getName() + " " + conn);
                // 唤醒其他等待的线程
                synchronized (this){
                    notifyAll();
                }
                break;
            }
        }
    }
}
