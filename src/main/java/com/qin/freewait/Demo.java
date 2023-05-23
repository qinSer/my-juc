package com.qin.freewait;

import java.sql.Connection;
import java.util.concurrent.TimeUnit;

/**
 * @Author qinSir
 * @Create 2023-05-19 15:24
 * @Description 测试自定义连接池demo
 */
public class Demo {
    public static void main(String[] args) {
        ConnPool connPool = new ConnPool(2);
        for (int i = 0; i < 4; i++) {
            new Thread(() ->{
                Connection conn = connPool.getConn();
                try {
                    TimeUnit.SECONDS.sleep(1L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                connPool.close(conn);
            },"t" + i).start();
        }
    }
}
