package com.qin.guarded;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * @Author qinSir
 * @Create 2023-05-16 8:58
 * @Description 测试类
 */
public class TestGuarded {
    public static void main(String[] args) throws InterruptedException {
        // 创建三个people用来收信
        for (int i = 0; i < 3; i++) {
            new People().start();
        }

        Set<Integer> ids = GuardedManager.getIds();
        // 因为是多线程，要保证下面步骤一定要在上面步骤之后执行，这里睡眠一下
        TimeUnit.SECONDS.sleep(1);
        // 送信
        for (Integer id : ids) {
            new PostMan(id,"送信内容：" + id).start();
        }
    }

    public void test(){
        // 多个线程需要使用同一个guardedObject对象
        GuardedObject guardedObject = new GuardedObject();
        // t1线程，用来获取结果
        new Thread(() -> {
            Object result = guardedObject.getResult(5000L);
            System.out.println("获取结果：" + result);
        },"t1").start();
        // t1线程，用来生成结果
        new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("t2线程生成结果中....");
            String res = "这是线程t2生成的结果";
            guardedObject.produceResult(res);
        },"t2").start();
    }
}
