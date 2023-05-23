package com.qin.poll;

import java.util.Deque;

/**
 * @Author qinSir
 * @Create 2023-05-20 9:44
 * @Description 当阻塞队列满了之后的拒绝策略
 */
@FunctionalInterface
public interface MyRejectPolicy<T> {
    public void process(BlockingQueue<T> queue, T task);
}
