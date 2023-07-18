//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.cf.utils.cache;

import com.alibaba.fastjson.TypeReference;
import com.cf.utils.log.LogHelper;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;

public class LocalCache implements ICache {
    private static Map<String, CacheObject> cacheMap = new ConcurrentHashMap();

    public LocalCache() {
    }

    protected void set(String key, CacheObject object) throws IllegalArgumentException {
        if (StringUtils.isEmpty(key)) {
            throw new IllegalArgumentException("设置缓存时key不能为空!");
        } else if (object.expireTime != null && object.expireTime.before(new Date())) {
            throw new IllegalArgumentException("缓存的过期时间不能早于当前时间!");
        } else {
            cacheMap.put(key, object);
        }
    }

    public void set(String key, Object value) {
        this.set(key, new CacheObject(value));
    }

    public void set(String key, Object value, Date expireTime) {
        this.set(key, new CacheObject(value, expireTime));
    }

    public void set(String key, Object value, int expireSecond) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(13, expireSecond);
        this.set(key, new CacheObject(value, calendar.getTime()));
    }

    public <T> T get(Class<T> t, String key) {
        return this.get(key);
    }

    public <T> T get(TypeReference<T> type, String key) {
        return this.get(key);
    }

    private <T> T get(String key) {
        T returnObject = null;
        CacheObject cacheObject = (CacheObject)cacheMap.get(key);
        if (cacheObject != null) {
            returnObject = cacheObject.getValue();
            if (cacheObject.getExpireTime() != null && cacheObject.getExpireTime().before(new Date())) {
                returnObject = null;
                cacheMap.remove(key);
            }
        }

        return returnObject;
    }

    public void remove(String key) {
        cacheMap.remove(key);
    }

    public synchronized boolean tryGetDistributedLock(String lockKey, String requestId, int expireTime) {
        String val = (String)this.get(String.class, lockKey);
        boolean bl = true;
        if (StringUtils.isNotBlank(val) && !"null".equals(val.toLowerCase())) {
            bl = false;
        } else {
            this.set(lockKey, requestId, expireTime / 1000);
        }

        LogHelper.debug(lockKey + "|加锁" + System.currentTimeMillis() + "^^^^^^^^^^^^^^^^^^^^^^^" + bl);
        return bl;
    }

    public boolean releaseDistributedLock(String lockKey, String requestId) {
        String val = (String)this.get(String.class, lockKey);
        boolean bl = false;
        if (!StringUtils.isBlank(val) && !"null".equals(val.toLowerCase())) {
            if (StringUtils.isNotBlank(requestId) && requestId.equals(val)) {
                this.remove(lockKey);
                bl = true;
            }
        } else {
            bl = true;
        }

        LogHelper.debug(lockKey + "|解锁" + System.currentTimeMillis() + "^^^^^^^^^^^^^^^^^^^^^^^" + bl);
        return bl;
    }

    public Long rPush(String key, int expireSecond, String... strs) {
        return null;
    }

    public String lPop(String key) {
        return null;
    }

    public Long lLen(String key) {
        return null;
    }

    public Long lPush(String key, int expireSecond, String... strs) {
        return null;
    }

    public List<String> lRange(String key, Long startPage, Long endPage) {
        return null;
    }

    public Long lRem(String key, int count, String str) {
        return null;
    }

    public List<String> sCan(String key) {
        return null;
    }

    public Long fuzzyDelKeys(List<String> keys) {
        return null;
    }

    public Long incr(String key) {
        return null;
    }

    public Boolean exists(String key) {
        return false;
    }

    public <T> List<T> getList(Class<T> t, String key) throws IOException {
        List<T> returnList = null;
        CacheObject cacheObject = (CacheObject)cacheMap.get(key);
        if (cacheObject != null) {
            returnList = (List)cacheObject.getValue();
            if (cacheObject.getExpireTime() != null && cacheObject.getExpireTime().before(new Date())) {
                returnList = null;
                cacheMap.remove(key);
            }
        }

        return returnList;
    }

    private class CacheObject {
        private Object value;
        private Date expireTime;

        public CacheObject(Object value) {
            this.value = value;
        }

        public CacheObject(Object value, Date expireTime) {
            this.value = value;
            this.expireTime = expireTime;
        }

        public Object getValue() {
            return this.value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public Date getExpireTime() {
            return this.expireTime;
        }

        public void setExpireTime(Date expireTime) {
            this.expireTime = expireTime;
        }
    }
}
