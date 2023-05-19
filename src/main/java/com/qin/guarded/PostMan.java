package com.qin.guarded;

/**
 * @Author qinSir
 * @Create 2023-05-16 10:09
 * @Description 具体描述
 */
public class PostMan extends Thread{
    // 需要给哪个ID的people送信
    private Integer id;
    // 送信内容
    private String msg;

    public PostMan(Integer id, String msg) {
        this.id = id;
        this.msg = msg;
    }

    @Override
    public void run() {
        System.out.println("开始送信....." + id );
        GuardedObject guardedObject = GuardedManager.getGuardedById(id);
        guardedObject.produceResult(msg);
    }
}
