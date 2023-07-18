//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.cf.utils.cache;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class CacheFactory {
    private static ICache localCache = new LocalCache();
    private static Map<Integer, ICache> redisCacheMap = new HashMap();

    public CacheFactory() {
    }

    public static ICache getLocalCache() {
        return localCache;
    }

    public static ICache getRedisCache(RedisConfig config) {
        if (config != null && !StringUtils.isEmpty(config.getHost())) {
            if (redisCacheMap.get(config.hashCode()) == null) {
                synchronized(redisCacheMap) {
                    if (redisCacheMap.get(config.hashCode()) == null) {
                        redisCacheMap.put(config.hashCode(), new RedisCache(config));
                    }
                }
            }

            return (ICache)redisCacheMap.get(config.hashCode());
        } else {
            return localCache;
        }
    }
}
