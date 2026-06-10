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
     * 字段显示名称
     */
    String value();

    /**
     * 是否深度比较嵌套对象
     * true: 递归比较对象内部的字段
     * false: 只比较对象本身（toString或null判断）
     */
    boolean deep() default false;

    /**
     * 比较深度（仅当 deep=true 时生效）
     * 1: 只比较直接嵌套的一层属性
     * 2: 比较两层嵌套
     * 3: 比较三层嵌套
     * 以此类推
     *
     * 注意：建议不要设置过大，避免性能问题
     */
    int depth() default 1;
}

