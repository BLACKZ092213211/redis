//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.cf.utils.cache;

import com.cf.utils.log.LogHelper;
import java.util.concurrent.ConcurrentHashMap;

public class LockMap {
    private static ConcurrentHashMap<String, ICache> lockMap = new ConcurrentHashMap();

    private LockMap() {
    }

    private static void lockReplenish() {
        try {
            if (lockMap.size() >= 1) {
                lockMap.forEach((k, v) -> {
                    v.set(k, v, 600);
                });
            }

            Thread.sleep(300000L);
        } catch (Exception var1) {
            LogHelper.error(var1);
        }

        lockReplenish();
    }

    public static void remove(String key) {
        lockMap.remove(key);
    }

    public static void put(String key, ICache cache) {
        lockMap.put(key, cache);
    }

    static {
        Thread t = new Thread(() -> {
            lockReplenish();
        });
        t.start();
    }
}
