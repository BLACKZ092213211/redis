//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.cf.utils.cache;

import com.alibaba.fastjson.TypeReference;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public interface ICache {
    void set(String var1, Object var2);

    Boolean exists(String var1);

    void set(String var1, Object var2, Date var3);

    Long incr(String var1);

    void set(String var1, Object var2, int var3);

    <T> T get(Class<T> var1, String var2);

    <T> List<T> getList(Class<T> var1, String var2) throws IOException;

    <T> T get(TypeReference<T> var1, String var2);

    void remove(String var1);

    boolean tryGetDistributedLock(String var1, String var2, int var3);

    boolean releaseDistributedLock(String var1, String var2);

    Long rPush(String var1, int var2, String... var3);

    String lPop(String var1);

    Long lLen(String var1);

    Long lPush(String var1, int var2, String... var3);

    List<String> lRange(String var1, Long var2, Long var3);

    Long lRem(String var1, int var2, String var3);

    List<String> sCan(String var1);

    Long fuzzyDelKeys(List<String> var1);
}
