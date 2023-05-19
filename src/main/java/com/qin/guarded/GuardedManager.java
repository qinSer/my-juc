package com.qin.guarded;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

/**
 * @Author qinSir
 * @Create 2023-05-16 9:57
 * @Description 用来管理 guardedObject，解耦生成线程和消费线程
 */
public class GuardedManager {
    // 使用map 进行存储 hashTable，线程安全的
    private static Map<Integer,GuardedObject> manager = new Hashtable<>();
    // 生成的id
    private static Integer id = 0;

    // 生成唯一ID
    public static synchronized Integer  genericId(){
        return id++;
    }

    // 生成guardedObject
    public static GuardedObject createGuarded(){
        Integer genericId = genericId();
        GuardedObject guardedObject = new GuardedObject(genericId);
        // 将生成的 guardedObject 对象交给 manager管理
        manager.put(genericId,guardedObject);
        return guardedObject;
    }

    // 获取所有的ids
    public static Set<Integer> getIds(){
        return manager.keySet();
    }

    // 根据id获取 对应的GuardedObject
    public static GuardedObject getGuardedById(Integer id){
        // 获取完后需要将其删除，因为他的任务已经完成了，不需要再占用空间了
        return manager.remove(id);
    }
}
