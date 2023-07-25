

package com.cf.pms.api.appactivity;

import com.cf.utils.log.LogHelper;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.cglib.core.Converter;
import org.springframework.util.CollectionUtils;

public class CachedBeanCopier {
    private static Logger logger = LogManager.getLogger(CachedBeanCopier.class);
    private static final Map<String, BeanCopier> BEAN_COPIERS = new HashMap();
    private static final Map<String, Converter> map = new HashMap();

    public CachedBeanCopier() {
    }

    public static <SourceType, TargetType> TargetType copyConvert(SourceType sourceObject, Class targetClass) {
        if (sourceObject != null && targetClass != null) {
            String key = genKey(sourceObject.getClass(), targetClass);
            BeanCopier copier;
            if (!BEAN_COPIERS.containsKey(key)) {
                copier = BeanCopier.create(sourceObject.getClass(), targetClass, true);
                BEAN_COPIERS.put(key, copier);
            } else {
                copier = (BeanCopier)BEAN_COPIERS.get(key);
            }

            Object converter;
            if (null != map.get("bean_convert")) {
                converter = (Converter)map.get("bean_convert");
            } else {
                converter = new BeanCopierConvertImpl();
                map.put("bean_convert", converter);
            }

            try {
                Object targetObject = targetClass.newInstance();
                copier.copy(sourceObject, targetObject, (Converter)converter);
                return targetObject;
            } catch (IllegalAccessException | InstantiationException var6) {
                LogHelper.exception(var6, logger, "对象类型转换异常，method={0}", new Object[]{"CachedBeanCopier.copyConvert()"});
                throw new RuntimeException(var6);
            }
        } else {
            return null;
        }
    }

    public static <OriginType, TargetType> List<TargetType> copyConvertList(List<OriginType> originObjs, Class targetTypeClass) {
        if (CollectionUtils.isEmpty(originObjs)) {
            return null;
        } else {
            List<TargetType> targetTypes = new ArrayList();
            Iterator var3 = originObjs.iterator();

            while(var3.hasNext()) {
                OriginType originType = var3.next();
                targetTypes.add(copyConvert(originType, targetTypeClass));
            }

            return targetTypes;
        }
    }

    private static String genKey(Class<?> sourceClazz, Class<?> targetClazz) {
        return sourceClazz.getName() + targetClazz.getName();
    }

    public static <TargetType, T> List<TargetType> meargeList(List<TargetType> masters, List<T> mearges, String masterIdName, String meargeIdName) {
        String idMasterGetter = "get" + masterIdName.substring(0, 1).toUpperCase() + masterIdName.substring(1, masterIdName.length());
        String idMeargeGetter = "get" + meargeIdName.substring(0, 1).toUpperCase() + meargeIdName.substring(1, meargeIdName.length());

        try {
            if (org.apache.commons.collections.CollectionUtils.isNotEmpty(masters)) {
                Iterator var6 = masters.iterator();

                while(true) {
                    while(true) {
                        Object master;
                        String masterId;
                        Class claMearges;
                        String fieldName;
                        do {
                            do {
                                if (!var6.hasNext()) {
                                    return masters;
                                }

                                master = var6.next();
                                Method getID = master.getClass().getMethod(idMasterGetter);
                                masterId = getID.invoke(master).toString();
                                claMearges = null;
                                if (org.apache.commons.collections.CollectionUtils.isNotEmpty(mearges)) {
                                    claMearges = mearges.get(0).getClass();
                                }

                                Class claz = master.getClass();
                                fieldName = null;
                                if (claMearges != null) {
                                    Field[] fields = claz.getDeclaredFields();
                                    Field[] var14 = fields;
                                    int var15 = fields.length;

                                    for(int var16 = 0; var16 < var15; ++var16) {
                                        Field field = var14[var16];
                                        if (field.getGenericType().getTypeName().equals(claMearges.getTypeName())) {
                                            fieldName = field.getName();
                                            break;
                                        }
                                    }
                                }
                            } while(claMearges == null);
                        } while(fieldName == null);

                        String setterFieldName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1, fieldName.length());
                        Method setterFieldMethod = master.getClass().getMethod(setterFieldName, claMearges);
                        Iterator var22 = mearges.iterator();

                        while(var22.hasNext()) {
                            T mearge = var22.next();
                            Method meargeIdGetter = mearge.getClass().getMethod(idMeargeGetter);
                            String meargeId = meargeIdGetter.invoke(mearge).toString();
                            if (meargeId != null && meargeId.equals(masterId)) {
                                setterFieldMethod.invoke(master, mearge);
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception var19) {
        }

        return masters;
    }
}
