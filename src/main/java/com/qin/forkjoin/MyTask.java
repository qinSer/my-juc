package com.qin.forkjoin;

import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

/**
 * @Author qinSir
 * @Create 2023-05-22 10:03
 * @Description 任务类，进行任务拆分
 *      继承RecursiveTask：存在返回值
 *      继承RecursiveAction:没有返回值
 * 测试 1-n之间的数字求和
 */
public class MyTask extends RecursiveTask<Integer> {

    private Integer begin;
    private Integer end;

    public MyTask(Integer begin, Integer end) {
        this.begin = begin;
        this.end = end;
    }

    @Override
    protected Integer compute() {
        // 如果 开始数字和结束数字相同，则返回开始数字
        if (begin.equals(end)){
            return begin;
        }
        // 如果开始数字和结束数字相差一，则没必要再进行拆分了，直接返回begin 和end 的和即可
        if (end.equals(begin+1)){
            return begin + end;
        }
        // 根据begin和end 的中间值进行拆分为两个任务
        int mid = (begin + end ) / 2;
        MyTask rightResult = new MyTask(begin, mid);
        // 执行任务
        rightResult.fork();
        MyTask leftResult = new MyTask(mid + 1, end);
        leftResult.fork();
        // 返回执行的结果
        return  rightResult.join() + leftResult.join();
    }
}
