package com.example.demo.config;

import com.alibaba.fastjson.TypeReference;
import com.cf.pms.core.redis.RedisRepository;
import com.cf.pms.model.redis.RedisConfig;
import com.cf.utils.cache.ICache;
import com.cf.utils.log.LogHelper;
import org.springframework.stereotype.Repository;

/**
 * @author mazy
 */
@Repository
public class RedisRepositoryImpl implements RedisRepository {

    @Override
    public void set(String key, Object value, int expireTime) {
        this.getCache().set(disposeKeyValue(key), value, expireTime);

    }

    @Override
    public <T> T get(Class<T> t, String key) {
        return this.getCache().get(t, disposeKeyValue(key));
    }

    @Override
    public <T> T get(TypeReference<T> t, String key) {
        return this.getCache().get(t, disposeKeyValue(key));
    }

    @Override
    public void remove(String key) {
        getCache().remove(disposeKeyValue(key));
    }

    @Override
    public void set(String key, Object value) {
        this.getCache().set(disposeKeyValue(key), value);
    }

    private String disposeKeyValue(String key) {
        return RedisConfig.getEnvironment() + "Q" + key;
    }

    /**
     * @Function: RedisRepositoryImpl.java
     * @Description: 获取一个缓存对象
     * @param:
     * @return：ICache
     * @author: mazy
     * @date: 2018年3月8日 下午8:12:36
     */
    private ICache getCache() {
        ICache cache = null;
        try {
            if (RedisConfig.getCacheType() != null && RedisConfig.getCacheType().booleanValue()) {
                cache = RedisConfig.getRedisCache();
            } else {
                cache = RedisConfig.getLocalCache();
            }
        } catch (Exception e) {
            LogHelper.error(e);
        }
        return cache;
    }

    @Override
    public boolean tryGetDistributedLock(String key, String uuid, int expireTime) {
        return getCache().tryGetDistributedLock(disposeKeyValue(key), uuid, expireTime);
    }

    @Override
    public boolean releaseDistributedLock(String key, String uuid) {
        return getCache().releaseDistributedLock(disposeKeyValue(key), uuid);
    }

}
