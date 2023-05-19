package com.qin.cas;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @Author qinSir
 * @Create 2023-05-19 8:53
 * @Description CAS 测试代码
 */
public class Demo {

    volatile String name = null;
    public static void main(String[] args) throws Exception {
//        testFileUpdater();
        testUnsafe();
    }

    /**
     * 测试UNSafe类,unsafe类对象只能通过反射进行获取： 原因
     *   private static final Unsafe theUnsafe;单例，并且没有对外暴露
     */
    public static void testUnsafe() throws Exception {
        //反射获取unsafe类单例属性
        Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
        //打破封装
        theUnsafe.setAccessible(true);
        //获取单例对象
        Unsafe unsafe = (Unsafe) theUnsafe.get(null);

        // 获取到unsafe后，对teacher对象进行操作
        Teacher teacher = new Teacher();
        // 获取teacher类中属性的偏移地址
        long idOffset = unsafe.objectFieldOffset(Teacher.class.getDeclaredField("id"));
        long nameOffset = unsafe.objectFieldOffset(Teacher.class.getDeclaredField("name"));
        /**
         * CAS 比较并交换 ，4个参数
         *  第一个参数：操作的对象
         *  第二个参数：操作的属性对应的偏移地址
         *  第三个参数：期望值
         *  第四个参数：需要修改的值
         */
        unsafe.compareAndSwapInt(teacher,idOffset,0,1);
        unsafe.compareAndSwapObject(teacher,nameOffset,null,"张三");

        System.out.println(teacher);
    }


    /**
     * 属性修改器，修改一个类中的指定属性，所修改的属性必须使用volatile修饰
     *  AtomicReferenceFieldUpdater // 域 字段
     *  AtomicIntegerFieldUpdater
     *  AtomicLongFieldUpdate
     */
    public static void testFileUpdater(){
        Demo demo = new Demo();
        AtomicReferenceFieldUpdater<Demo, String> fieldUpdater =
                AtomicReferenceFieldUpdater.newUpdater(Demo.class, String.class, "name");
        fieldUpdater.compareAndSet(demo,null,"张三");
        System.out.println(demo.name);
    }

    /**
     * 测试计数累加器,计数累加器相比于AtomicInteger中的增加和减少，效率要高
     *  DoubleAdder
     *  DoubleAccumulator
     *  LongAccumulator
     *  LongAdder
     */
    public void testAdder(){
        LongAdder longAdder = new LongAdder();
        longAdder.increment();
    }
    public static void testArray() throws InterruptedException {
        demo(
                () -> new int[10],
                (array) -> array.length,
                (array,index) -> array[index]++,
                (array) -> System.out.println("普通数组：" + Arrays.toString(array))
        );
        demo(
                () -> new AtomicIntegerArray(10),
                (array) -> array.length(),
                (array,index) -> array.getAndIncrement(index),
                (array) -> System.out.println("原子数组：" + array)
        );
    }
    /**
     * 创建一个数组，并对其中的数据进行操作
     * 普通数组存在数据安全问题
     * @param supplier 提供一个数组
     * @param function 根据数组，获取其length
     * @param biConsumer 传递数组和一个索引，对其中的数据进行处理
     * @param consumer 打印整个数组
     * @param <T>
     * @throws InterruptedException
     *  AtomicIntegerArray
     *  AtomicLongArray
     *  AtomicReferenceArray
     */
    public static <T> void demo(Supplier<T> supplier,
                                Function<T,Integer> function,
                                BiConsumer<T,Integer> biConsumer,
                                Consumer<T> consumer) throws InterruptedException {
        List<Thread> list = new ArrayList<>();
        T t = supplier.get();
        Integer length = function.apply(t);
        for (int i = 0; i < length; i++) {
            Thread thread = new Thread(() -> {
                for (int j = 0; j < 10000; j++) {
                    biConsumer.accept(t, j % length);
                }
            });
            list.add(thread);
            thread.start();
        }
        for (int i = 0; i < length; i++) {
            list.get(i).join();
        }
        consumer.accept(t);
    }

    /**
     * 测试ABA问题
     * @throws InterruptedException
     *  AtomicReference:可以解决对象的数据安全问题，存在ABA问题
     *  AtomicStampedReference：可以解决对象的数据安全问题,并且可以记录一个版本号，解决ABA问题
     *  AtomicMarkableReference:可以解决对象的数据安全问题,并且可以记录一个布尔值，解决ABA问题
     */
    public void testABA() throws InterruptedException{
        // 根据版本号，避免CAS的ABA问题
        AtomicStampedReference<String> reference =
                new AtomicStampedReference<String>("A",0);
        String pre;
        String next;
        int stamp;
        int newStamp;
        do {
            // 获取值
            pre = reference.getReference();
            // 获取版本号
            stamp = reference.getStamp();
            // 修改成功后，版本号加一
            newStamp = stamp + 1;
            // 将要修改的值
            next = pre + "next";
        } while (!reference.compareAndSet(pre,next,stamp,newStamp));

        TimeUnit.SECONDS.sleep(1);
        System.out.println("修改后的值：" + reference.getReference());
        System.out.println("修改后的版本号：" + reference.getStamp());
    }

    /**
     * 解决 数值型的数据安全问题
     * @throws InterruptedException
     *  AtomicInteger:解决整数型
     *  AtomicLong:解决长整数型
     *  AtomicBoolean:解决布尔型
     */
    public void test() throws InterruptedException{
        // 使用原子整数，进行数据的减操作
        AtomicInteger ai = new AtomicInteger(200);
        for (int i = 0; i < 10 ; i++) {
            new Thread(() -> {
                int target;
                int next;
                //使用CAS保证数据的安全
                do {
                    target = ai.get();
                    next = target - 20;
                }while (!ai.compareAndSet(target,next));
                System.out.println(Thread.currentThread().getName() + "：" + next);
            },"t" + i).start();
        }
        TimeUnit.SECONDS.sleep(1);
        System.out.println(ai.get());
    }
}
