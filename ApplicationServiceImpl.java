/*
 * ApplicationServiceImpl.java 1.0.0 2018/7/25  下午5:59
 * Copyright © 2014-2017,52mamahome.com.All rights reserved
 * history :
 *     1. 2018/7/25  下午5:59 created by yinqiang
 */
package com.cf.pms.service.impl.common;

import com.cf.pms.service.common.ApplicationService;
import org.springframework.beans.BeansException;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.stereotype.Service;

@Service
public class ApplicationServiceImpl extends ApplicationObjectSupport implements ApplicationService {

    @Override
    public String getCurrentResourceClassPath() {
        return this.getApplicationContext().getClassLoader().getResource("").getPath();
    }

    @Override
    public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
        return this.getApplicationContext().getBean(name, requiredType);
    }
}
