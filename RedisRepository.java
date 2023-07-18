package com.example.demo.config;

import com.alibaba.fastjson.TypeReference;

public interface RedisRepository {
    /**
     * @Function: RedisRepository.java
     * @Description: 数据写入，过期时间（秒）
     * @param:key：键,value:值，expireTime:时间（秒）
     * @return：void
     * @author: mazy
     * @date: 2018年3月8日 下午8:04:12
     */
    void set(String key, Object value, int expireTime);

    /**
     * @Function: RedisRepository.java
     * @Description: 该函数的功能描述
     * @param:TODO
     * @return：T
     * @author: mazy
     * @date: 2018年3月8日 下午8:08:45
     */
    <T> T get(Class<T> t, String key);

    /**
     * 获取一个缓存项 （集合）
     *
     * @param t
     * @param key
     * @param <T>
     * @return
     */
    <T> T get(TypeReference<T> t, String key);

    /**
     * 删除一个缓存项
     *
     * @param key
     */
    void remove(String key);

    /**
     * 设置一个缓存,无过期时间
     *
     * @param key   缓存key
     * @param value 缓存值
     */
    void set(String key, Object value);

    /**
     * @Function: RedisRepository.java
     * @Description: 分布式加锁
     * @key:键
     * @uuid:唯一ID
     * @expireTime:多少毫秒后失效
     * @return：boolean
     * @author: mazy
     * @date: 2018年11月21日 下午6:07:35
     */
    boolean tryGetDistributedLock(String key, String uuid, int expireTime);

    /**
     * @Function: RedisRepository.java
     * @Description: 释放锁
     * @key:键
     * @uuid:值
     * @return：boolean
     * @author: mazy
     * @date: 2018年11月21日 下午6:09:31
     */
    boolean releaseDistributedLock(String key, String uuid);


}
