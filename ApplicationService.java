/*
 * ApplicationService.java 1.0.0 2018/7/25  下午5:58
 * Copyright © 2014-2017,52mamahome.com.All rights reserved
 * history :
 *     1. 2018/7/25  下午5:58 created by yinqiang
 */
package com.cf.pms.service.common;

import org.springframework.beans.BeansException;
import org.springframework.lang.Nullable;

/**
 * 系统相关服务
 */
public interface ApplicationService {

    /**
     * 获取当前系统资源文件路径
     *
     * @return
     */
    String getCurrentResourceClassPath();

    <T> T getBean(String name, @Nullable Class<T> requiredType) throws BeansException;

}
