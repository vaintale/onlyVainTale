package com.vaintale.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 属性 setter 缓存工具类，用于替代低效的 BeanUtils.setProperty
 * 在循环中使用，特定条件下，无法一次性获取源数据全部属性值，则使用此缓存工具类
 *
 * @author vaintale
 * @date 2026/6/9
 */
public class PropertySetterCacheUtil {
    private static final Logger log = LoggerFactory.getLogger(PropertySetterCacheUtil.class);

    // 缓存 Class -> fieldName -> setter Method
    private static final Map<Class<?>, Map<String, Method>> CACHE = new ConcurrentHashMap<>();

    /**
     * 初始化指定类的所有 setter 方法
     *
     * @param clazz 类型
     */
    public static void initialize(Class<?> clazz) {
        if (CACHE.containsKey(clazz)) return;

        Map<String, Method> methodMap = new ConcurrentHashMap<>();
        for (Method method : clazz.getMethods()) {
            if (method.getName().startsWith("set") && method.getParameterCount() == 1) {
                String propertyName = method.getName().substring(3).toLowerCase();
                methodMap.put(propertyName, method);
            }
        }
        CACHE.put(clazz, methodMap);
    }

    /**
     * 获取指定类和属性名对应的 setter 方法
     *
     * @param clazz 类型
     * @param key   属性名（驼峰或下划线格式）
     * @return setter 方法
     */
    public static Method getSetter(Class<?> clazz, String key) {
        initialize(clazz);
        return CACHE.get(clazz).get(key.toLowerCase());
    }

    /**
     * 调用对象的 setter 方法设置值
     *
     * @param obj   目标对象
     * @param key   属性名
     * @param value 值
     * @return 是否成功调用
     */
    public static boolean invokeSetter(Object obj, String key, Object value) {
        try {
            Method method = getSetter(obj.getClass(), key);
            if (method != null && value != null && method.getParameterTypes()[0].isAssignableFrom(value.getClass())) {
                method.invoke(obj, value);
                return true;
            }
        } catch (Exception e) {
            log.error("调用对象 {} 的 setter 方法失败: {}", obj.getClass().getSimpleName(), key);
        }
        return false;
    }
}
