package com.qin.aqs;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @Author qinSir
 * @Create 2023-05-22 10:51
 * @Description 自定义锁，使用 AQS（abstractQueuedSynchronizer） 实现
 *      继承Lock锁，实现接口规范
 */
public class MyLock implements Lock {
    private MyAQS aqs = new MyAQS();
    class MyAQS extends AbstractQueuedSynchronizer {
        // 获取锁
        @Override
        protected boolean tryAcquire(int arg) {
            // 如果可以将state状态改为 arg，则表示获取到了锁，
            if (compareAndSetState(0,arg)){
                // 设置持有锁的线程
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        // 释放锁
        @Override
        protected boolean tryRelease(int arg) {
            // 设置持有锁的线程
            setExclusiveOwnerThread(null);
            // 设置状态state
            // 注意： state 的设置必须在setExclusiveOwnerThread之下，因为state使用了volatile关键字
            // 可以在setState写之后添加写屏障，同步之前的数据到主存，并且禁止指令重排序
            setState(arg);
            return true;
        }

        // 判断是否持有锁
        @Override
        protected boolean isHeldExclusively() {
            return getState() == 1;
        }

        // 获取条件变量
        public Condition getCondition(){
            return new ConditionObject();
        }
    }

    // 加锁，不可打断
    @Override
    public void lock() {
        while (!aqs.tryAcquire(1)){
            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
    // 加锁，可打断
    @Override
    public void lockInterruptibly() throws InterruptedException {
        aqs.acquireInterruptibly(1);
    }

    // 尝试加锁
    @Override
    public boolean tryLock() {
        return aqs.tryAcquire(1);
    }

    // 尝试加锁，设置超时时间
    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return aqs.tryAcquireNanos(1,unit.toNanos(time));
    }
    // 解锁
    @Override
    public void unlock() {
        // 需要调用release方法进行解锁，底层调用tryRelease方法，并且在解锁完成之后会唤醒其他线程
        aqs.release(0);
    }
    // 获取条件变量
    @Override
    public Condition newCondition() {
        return aqs.getCondition();
    }
}
