package com.qin.poll;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author qinSir
 * @Create 2023-05-20 9:41
 * @Description 阻塞对列，线程池中的任务满了之后，需要将新增的任务加入阻塞队列
 */
public class BlockingQueue<T> {
    // 阻塞队列大小
    private Integer size;
    // 使用链表模拟队列
    private Deque<T> queue = new ArrayDeque<>();
    // 锁
    private Lock lock = new ReentrantLock();
    // 添加任务的等待条件 : 队列满了，存放任务的线程需要等待
    private Condition putCondition = lock.newCondition();
    // 获取任务的等待条件 ： 队列空了，获取任务的线程需要等待
    private Condition pollCondition = lock.newCondition();

    public BlockingQueue(Integer size) {
        this.size = size;
    }

    // 将任务存放到队列中 ，如果队列满了无限时等待
    public boolean put(T task){
        // 加锁
        lock.lock();
        try {
            // 使用while，避免虚假唤醒,如果队列满了，则进入等待
            while (queue.size() == size){
                try {
                    System.out.println(Thread.currentThread().getName() + "：等待存储任务：" + task);
                    putCondition.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.println(Thread.currentThread().getName() + "：成功存储任务：" + task);
            // 存储任务
            queue.addLast(task);
            // 唤醒获取任务的队列
            pollCondition.signal();
            return true;
        }finally {
            lock.unlock();
        }
    }
    // 将任务存放到队列中 ，超时时间等待
    public boolean put(T task, Long timeout, TimeUnit timeUnit){
        // 加锁
        lock.lock();
        try {
            long nanos = timeUnit.toNanos(timeout);
            // 使用while，避免虚假唤醒,如果队列满了，则进入等待
            while (queue.size() == size){
                try {
                    if (nanos <= 0){
                        System.out.println(Thread.currentThread().getName() + "：存储任务超时：" + task);
                        // 超时了，添加失败，返回FALSE
                        return false;
                    }
                    System.out.println(Thread.currentThread().getName() + "：等待存储任务：" + task);
                    nanos = putCondition.awaitNanos(nanos);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.println(Thread.currentThread().getName() + "：成功存储任务：" + task);
            // 存储任务
            queue.addLast(task);
            // 唤醒获取任务的队列
            pollCondition.signal();
            return true;
        }finally {
            lock.unlock();
        }
    }
    // 获取任务队列的任务，队列空了，则进入无限时等待
    public T poll(){
        lock.lock();
        try {
            // 避免虚假唤醒，使用while，队列空了，则进入无限时等待
            while (queue.size() == 0){
                try {
                    System.out.println(Thread.currentThread().getName() + "：等待获取任务" );
                    pollCondition.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            T runnable = queue.removeFirst();
            System.out.println(Thread.currentThread().getName() + "：成功获取任务:" +runnable );
            // 唤醒存储任务的线程
            putCondition.signal();
            return runnable;
        }finally {
            lock.unlock();
        }
    }
    // 获取任务队列的任务，队列空了，则进入无限时等待
    public T poll(Long timeout,TimeUnit timeUnit){
        lock.lock();
        try {
            long nanos = timeUnit.toNanos(timeout);
            // 避免虚假唤醒，使用while，队列空了，则进入无限时等待
            while (queue.size() == 0){
                try {
                    if (nanos <= 0){
                        System.out.println(Thread.currentThread().getName() + "：等待获取任务超时" );
                        return null;
                    }
                    System.out.println(Thread.currentThread().getName() + "：等待获取任务" );
                    nanos = pollCondition.awaitNanos(nanos);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            T runnable = queue.removeFirst();
            System.out.println(Thread.currentThread().getName() + "：成功获取任务:" +runnable );
            // 唤醒存储任务的线程
            putCondition.signal();
            return runnable;
        }finally {
            lock.unlock();
        }
    }

    // 获取当前阻塞队列容量
    public int size(){
        lock.lock();
        try {
            return queue.size();
        }finally {
            lock.unlock();
        }
    }

    // 使用拒绝策略解决等待问题
    public void tryPut(MyRejectPolicy rejectPolicy,T task) {
        lock.lock();
        try {
            // 如果队列满了，调用拒绝策略解决
            if (size() == size){
                rejectPolicy.process(this,task);
            }else {
                // 如果没有满，则添加
                System.out.println(Thread.currentThread().getName() + "：成功添加任务:" + task );
                queue.addLast(task);
                pollCondition.signal();
            }
        }finally {
            lock.unlock();
        }
    }
}
