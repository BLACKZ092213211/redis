package com.example.demo.config;

import com.cf.utils.cache.LocalCache;
import com.cf.utils.cache.RedisCache;
import com.cf.utils.log.LogHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

@Configuration
public class RedisConfig {

    private static Boolean cacheType;

    private static Integer maxIdle;

    private static Integer maxTotal;

    private static Boolean testOnBorrow;

    private static Boolean testOnReturn;

    private static String host;

    private static Integer port;

    private static String userName;

    private static String password;

    private static RedisCache redisCache = null;

    private static LocalCache localCache = null;

    private static Object lockObj = new Object();

    private static String environment;

    public static Boolean getCacheType() throws Exception {
        if (cacheType == null) {
            setConfig();
        }
        return cacheType;
    }

    public static Integer getMaxIdle() {
        return maxIdle;
    }

    public static Integer getMaxTotal() {
        return maxTotal;
    }

    public static Boolean getTestOnBorrow() {
        return testOnBorrow;
    }

    public static Boolean getTestOnReturn() {
        return testOnReturn;
    }

    public static String getHost() {
        return host;
    }

    public static Integer getPort() {
        return port;
    }

    public static String getUserName() {
        return userName;
    }

    public static String getPassword() {
        return password;
    }

    public static RedisCache getRedisCache() {
        produce();
        return redisCache;
    }

    public static LocalCache getLocalCache() {
        produce();
        return localCache;
    }

    public static String getEnvironment() {
        return environment;
    }

    @Bean
    public static boolean setConfig() throws Exception {

        cacheType = false;
        boolean setResult = true;
        String rootPath = System.getProperty("catalina.home");
        if (rootPath == null) {
            LogHelper.info("RedisConfig.setConfig():**************************************没有获取到catalina.home目录 **************************************");
            return setResult;
        }

        String separator = System.getProperty("file.separator");
        if (StringUtils.isEmpty(rootPath)) {
            rootPath = System.getProperty("catalina.base");
        }
        String path = rootPath + separator + "webconfigs" + separator + "pmsapi" + separator + "cach.properties";
        File file = new File(path);
        if (!file.exists()) {
            LogHelper.info("RedisConfig.setConfig():没有加载到redis的缓存文件直接启用本地缓存配置**************************************");
            return setResult;
        }

        FileInputStream in = new FileInputStream(file);
        // ClassPathResource resource = new ClassPathResource(path);
        Properties properties = new Properties();
        properties.load(in);
        in.close();
        cacheType = Boolean.valueOf(properties.getProperty("cacheType"));
        maxIdle = Integer.parseInt(properties.getProperty("maxIdle"));
        maxTotal = Integer.parseInt(properties.getProperty("maxTotal"));
        testOnBorrow = Boolean.valueOf(properties.getProperty("testOnBorrow"));
        testOnReturn = Boolean.valueOf(properties.getProperty("testOnReturn"));
        host = properties.getProperty("host");
        port = Integer.parseInt(properties.getProperty("port"));
        userName = properties.getProperty("userName");
        password = properties.getProperty("password");
        environment = properties.getProperty("environment");
        return setResult;
    }

    private static void produce() {

        if (cacheType) {
            // 生产
            if (redisCache == null) {
                synchronized (lockObj) {
                    if (redisCache == null) {
                        com.cf.utils.cache.RedisConfig config = new com.cf.utils.cache.RedisConfig();
                        config.setMaxIdle(maxIdle);
                        config.setMaxTotal(maxTotal);
                        config.setTestOnBorrow(testOnBorrow);
                        config.setTestOnReturn(testOnReturn);
                        config.setHost(host);
                        config.setPort(port);
                        config.setUserName(userName);
                        config.setPassword(password);
                        redisCache = new RedisCache(config);
                    }
                }

            }
        } else {
            // 本地
            if (localCache == null) {
                synchronized (lockObj) {
                    if (localCache == null) {
                        localCache = new LocalCache();
                    }
                }

            }
        }
    }

}
