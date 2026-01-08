package com.vaintale.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * @author vaintale
 * @date 2025/9/5
 */


public class IPAddressValidator implements ConstraintValidator<ValidIP, String> {

    private static final String IPV4_PATTERN =
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}" +
                    "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    private static final String IPV6_PATTERN =
            "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$|" +
                    "^::1$|^::$|^([0-9a-fA-F]{1,4}:){1,7}:|" +
                    "^([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}$";

    private static final Pattern IPV4_PATTERN_PATT = Pattern.compile(IPV4_PATTERN);
    private static final Pattern IPV6_PATTERN_PATT = Pattern.compile(IPV6_PATTERN);

    private ValidIP.IPType ipType;
    private boolean allowEmpty;

    @Override
    public void initialize(ValidIP constraintAnnotation) {
        this.ipType = constraintAnnotation.type();
        this.allowEmpty = constraintAnnotation.allowEmpty();
    }

    @Override
    public boolean isValid(String ipAddress, ConstraintValidatorContext context) {
        if (ipAddress == null) {
            return allowEmpty;
        }

        String trimmedIp = ipAddress.trim();
        if (trimmedIp.isEmpty()) {
            return allowEmpty;
        }

        switch (ipType) {
            case IPv4:
                return isValidIPv4(trimmedIp);
            case IPv6:
                return isValidIPv6(trimmedIp);
            case ANY:
                return isValidIPv4(trimmedIp) || isValidIPv6(trimmedIp);
            default:
                return false;
        }
    }

    private boolean isValidIPv4(String ipAddress) {
        return IPV4_PATTERN_PATT.matcher(ipAddress).matches();
    }

    private boolean isValidIPv6(String ipAddress) {
        return IPV6_PATTERN_PATT.matcher(ipAddress).matches();
    }
}