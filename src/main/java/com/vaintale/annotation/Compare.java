package com.vaintale.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 实体类比较注解
 * 用于标记需要比较的字段
 *
 * @author vaintale
 * @date 2025/2/22
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Compare {

    /**
     * 字段名称
     */
    String value();
}

