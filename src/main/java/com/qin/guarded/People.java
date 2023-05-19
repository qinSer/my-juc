package com.qin.guarded;

/**
 * @Author qinSir
 * @Create 2023-05-16 10:05
 * @Description 具体描述
 */
public class People extends Thread{
    @Override
    public void run() {
        GuardedObject guarded = GuardedManager.createGuarded();
        System.out.println("开始收信........." + guarded.getId());
        Object result = guarded.getResult();
        System.out.println("收到信：" + result);
    }
}
