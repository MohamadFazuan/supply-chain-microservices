package com.supply.chain.microservice.common.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods for execution time logging
 * Add to: common-lib/src/main/java/com/supply/chain/microservice/common/logging/
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogExecutionTime {
    String value() default "";
    boolean includeArgs() default false;
    boolean includeResult() default false;
}
