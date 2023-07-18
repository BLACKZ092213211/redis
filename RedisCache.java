//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.cf.utils.cache;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.Feature;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

public class RedisCache implements ICache {
    private static final String LOCK_SUCCESS = "OK";
    private static final String SET_IF_NOT_EXIST = "NX";
    private static final String SET_WITH_EXPIRE_TIME = "PX";
    private static final Long RELEASE_SUCCESS = 1L;
    private JedisPool jedisPool;

    private RedisCache() {
    }

    public RedisCache(RedisConfig redisConfig) {
        if (redisConfig == null) {
            throw new IllegalArgumentException("使用redis时,redisConfig不能为空!");
        } else {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxIdle(redisConfig.getMaxIdle());
            config.setMaxTotal(redisConfig.getMaxTotal());
            config.setTestOnBorrow(redisConfig.isTestOnBorrow());
            config.setTestOnReturn(redisConfig.isTestOnReturn());
            String password = null;
            if ("auth".equals(redisConfig.getUserName())) {
                password = redisConfig.getPassword();
            } else {
                password = String.format("%s:%s", redisConfig.getUserName(), redisConfig.getPassword());
            }

            this.jedisPool = new JedisPool(config, redisConfig.getHost(), redisConfig.getPort(), 3000, password);
        }
    }

    public void set(String key, Object value) {
        this.set(key, value, (Date)null);
    }

    public Boolean exists(String key) {
        Jedis jedis = null;

        Boolean var3;
        try {
            jedis = this.jedisPool.getResource();
            var3 = jedis.exists(key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }

        }

        return var3;
    }

    public void set(String key, Object value, Date expireTime) {
        if (expireTime != null && expireTime.before(new Date())) {
            throw new IllegalArgumentException("缓存的过期时间不能早于当前时间!");
        } else {
            int expireSecond = 0;
            if (expireTime != null) {
                expireSecond = (int)(expireTime.getTime() - (new Date()).getTime()) / 1000;
            }

            this.set(key, value, expireSecond);
        }
    }

    public Long incr(String key) {
        if (StringUtils.isEmpty(key)) {
            throw new IllegalArgumentException("设置缓存时key不能为空!");
        } else {
            Jedis jedis = null;

            Long var3;
            try {
                jedis = this.jedisPool.getResource();
                var3 = jedis.incr(key);
            } finally {
                if (jedis != null) {
                    jedis.close();
                }

            }

            return var3;
        }
    }

    public void set(String key, Object value, int expireSecond) {
        if (StringUtils.isEmpty(key)) {
            throw new IllegalArgumentException("设置缓存时key不能为空!");
        } else {
            Jedis jedis = null;

            try {
                String stringValue;
                if (value instanceof String) {
                    stringValue = value.toString();
                } else {
                    stringValue = JSON.toJSONString(value);
                }

                jedis = this.jedisPool.getResource();
                jedis.set(key, stringValue);
                if (expireSecond > 0) {
                    jedis.expire(key, expireSecond);
                }
            } finally {
                if (jedis != null) {
                    jedis.close();
                }

            }

        }
    }

    public <T> T get(Class<T> t, String key) {
        T returnObject = null;
        Jedis jedis = null;

        try {
            jedis = this.jedisPool.getResource();
            String value = jedis.get(key);
            if (StringUtils.isNotBlank(value) && !"null".equals(value.toLowerCase())) {
                if (isjson(value)) {
                    returnObject = JSON.parseObject(value.getBytes(), t, new Feature[0]);
                } else {
                    returnObject = value;
                }
            }
        } finally {
            if (jedis != null) {
                jedis.close();
            }

        }

        return returnObject;
    }

    private static boolean isjson(String s) {
        boolean bl = false;
        if (s.contains("{") && s.contains("}")) {
            bl = true;
        } else if (s.contains("[") && s.contains("]")) {
            bl = true;
        }

        return bl;
    }

    public <T> T get(TypeReference<T> type, String key) {
        T returnObject = null;
        Jedis jedis = null;

        try {
            jedis = this.jedisPool.getResource();
            String value = jedis.get(key);
            if (value != null) {
                returnObject = JSON.parseObject(value, type, new Feature[0]);
            }
        } finally {
            if (jedis != null) {
                jedis.close();
            }

        }

        return returnObject;
    }

    public void remove(String key) {
        Jedis jedis = null;

        try {
            jedis = this.jedisPool.getResource();
            jedis.del(key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }

        }

    }

    public boolean tryGetDistributedLock(String lockKey, String requestId, int expireTime) {
        Jedis jedis = null;

        boolean var6;
        try {
            jedis = this.jedisPool.getResource();
            String result = jedis.set(lockKey, requestId, "NX", "PX", expireTime);
            if (!StringUtils.isNotBlank(result) || !"OK".equals(result.toUpperCase())) {
                return false;
            }

            var6 = true;
        } finally {
            if (jedis != null) {
                jedis.close();
            }

        }

        return var6;
    }

    public boolean releaseDistributedLock(String lockKey, String requestId) {
        Jedis jedis = null;

        try {
            jedis = this.jedisPool.getResource();
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            Object result = jedis.eval(script, Collections.singletonList(lockKey), Collections.singletonList(requestId));
            if (RELEASE_SUCCESS.equals(result)) {
                boolean var6 = true;
                return var6;
            }
        } finally {
            if (jedis != null) {
                jedis.close();
            }

        }

        return false;
    }

    public Long rPush(String key, int expireSecond, String... strs) {
        Jedis jedis = null;
        Long res = null;

        try {
            jedis = this.jedisPool.getResource();
            res = jedis.rpush(key, strs);
            if (expireSecond > 0) {
                jedis.expire(key, expireSecond);
            }
        } finally {
            if (jedis != null) {
                jedis.close();
            }

        }

        return res;
    }

    public synchronized String lPop(String key) {
        Jedis jedis = null;
        String res = null;

        try {
            jedis = this.jedisPool.getResource();
            res = jedis.lpop(key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }

        }

        return res;
    }

    public Long lLen(String key) {
        Jedis jedis = null;
        Long res = 0L;

        try {
            jedis = this.jedisPool.getResource();
            res = jedis.llen(key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }

        }

        return res;
    }

    public Long lPush(String key, int expireSecond, String... strs) {
        Jedis jedis = null;
        Long res = null;

        try {
            jedis = this.jedisPool.getResource();
            res = jedis.lpush(key, strs);
            if (expireSecond > 0) {
                jedis.expire(key, expireSecond);
            }
        } finally {
            if (jedis != null) {
                jedis.close();
            }

        }

        return res;
    }

    public List<String> lRange(String key, Long startPage, Long endPage) {
        Jedis jedis = null;
        List<String> res = null;

        try {
            jedis = this.jedisPool.getResource();
            res = jedis.lrange(key, startPage, endPage);
        } finally {
            if (jedis != null) {
                jedis.close();
            }

        }

        return res;
    }

    public Long lRem(String key, int count, String str) {
        Jedis jedis = null;
        Long res = null;

        try {
            jedis = this.jedisPool.getResource();
            res = jedis.lrem(key, (long)count, str);
        } finally {
            if (jedis != null) {
                jedis.close();
            }

        }

        return res;
    }

    public List<String> sCan(String key) {
        Jedis jedis = null;
        List<String> keysList = new ArrayList();

        try {
            jedis = this.jedisPool.getResource();
            ScanParams paramas = new ScanParams();
            paramas.match(key);
            paramas.count(1000);
            String cursor = "0";

            do {
                ScanResult<String> scanResult = jedis.scan(cursor, paramas);
                List<String> selectedElements = scanResult.getResult();
                if (selectedElements != null && !selectedElements.isEmpty()) {
                    keysList.addAll(selectedElements);
                    cursor = scanResult.getStringCursor();
                }
            } while(!"0".equals(cursor));
        } finally {
            if (jedis != null) {
                jedis.close();
            }

        }

        return keysList;
    }

    public Long fuzzyDelKeys(List<String> keys) {
        Jedis jedis = null;
        Long res = null;

        try {
            jedis = this.jedisPool.getResource();
            if (CollectionUtils.isNotEmpty(keys)) {
                String[] array = (String[])keys.toArray(new String[0]);
                res = jedis.del(array);
            }
        } finally {
            if (jedis != null) {
                jedis.close();
            }

        }

        return res;
    }

    public <T> List<T> getList(Class<T> t, String key) throws IOException {
        List<T> returnList = null;
        Jedis jedis = null;

        try {
            jedis = this.jedisPool.getResource();
            String value = jedis.get(key);
            if (StringUtils.isNotBlank(value) && !"null".equals(value.toLowerCase())) {
                if (!isjson(value)) {
                    throw new IOException(value);
                }

                returnList = JSON.parseArray(value, t);
            }
        } finally {
            if (jedis != null) {
                jedis.close();
            }

        }

        return returnList;
    }
}
