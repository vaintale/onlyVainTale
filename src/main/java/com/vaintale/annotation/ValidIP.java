package com.vaintale.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * @author vaintale
 * @date 2025/9/5
 */
@Documented
@Constraint(validatedBy = IPAddressValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidIP {

    String message() default "无效的IP地址格式";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    // 可选：支持IPv4、IPv6或两者都支持
    IPType type() default IPType.ANY;

    // 可选：是否允许空值
    boolean allowEmpty() default false;

    enum IPType {
        IPv4, IPv6, ANY
    }
}
