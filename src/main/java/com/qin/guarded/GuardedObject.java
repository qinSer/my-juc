package com.qin.guarded;

import jdk.nashorn.internal.runtime.regexp.JoniRegExp;

/**
 * @Author qinSir
 * @Create 2023-05-16 8:57
 * @Description
 *  即 Guarded Suspension，用在一个线程等待另一个线程的执行结果
 *      有一个结果需要从一个线程传递到另一个线程，让他们关联同一个 GuardedObject
 *      如果有结果不断从一个线程到另一个线程那么可以使用消息队列（见生产者/消费者）
 *      JDK 中，join 的实现、Future 的实现，采用的就是此模式
 *      因为要等待另一方的结果，因此归类到同步模式
 */
public class GuardedObject {
    // 唯一标识
    private Integer id;
    /**
     * 存储最终结果
      */
    private Object result = null;
    /**
     * 标志位，判断线程是否执行结束，需要返回结果
      */
    private Boolean flag = false;

    public Integer getId() {
        return id;
    }

    public GuardedObject() {
    }

    public GuardedObject(Integer id) {
        this.id = id;
    }

    /**
     * 获取线程产生的结果
     * @return 返回result结果
     * 因为存在读写操作，避免读操作时，写操作在进行而导致脏读问题，需要加锁
     */
    public Object getResult(){
        return getResult(0L);
    }
    /**
     * 获取线程产生的结果
     * @return 返回result结果
     * 因为存在读写操作，避免读操作时，写操作在进行而导致脏读问题，需要加锁
     * @param time 超时时间，单位 毫秒
     */
    public synchronized Object getResult(Long time){
        //记录一下当前时间
        long currentTimeMillis = System.currentTimeMillis();
        //记录线程被唤醒时间
        long notifyTime = 0;
        if (time == 0L){
            // 避免虚假唤醒，使用while替换if
            while (!flag){
                try {
                    // 进入waitting状态，进行等待，释放锁
                    wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }else{
            // 避免虚假唤醒，使用while替换if
            while (!flag){
                try {
                    // 需要睡眠的时间， 避免线程被虚假唤醒，睡眠时间需要减去上次睡眠的时间
                    long waitTime = time - notifyTime;
                    if (waitTime <= 0){
                        break;
                    }
                    // 进入waitting状态，进行等待，释放锁
                    wait(waitTime);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                // 如果wait被唤醒了，则进行记录时间
                notifyTime = System.currentTimeMillis() - currentTimeMillis;
            }
        }
        //标志位设置为FALSE
        flag = false;
        return result;
    }

    /**
     * @param result 另一个线程传递过来的结果值
     * 设置结果值，和标志位
     */
    public synchronized void produceResult(Object result){
        //设置结果
        this.result = result;
        //设置标志位
        this.flag = true;
        //唤醒其他等待的线程
        notifyAll();
    }
}
