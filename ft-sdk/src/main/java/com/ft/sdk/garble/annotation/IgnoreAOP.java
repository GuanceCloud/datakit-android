package com.ft.sdk.garble.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * annotation 标记，用于 ft-plugin 自动采集做标记区分，用 IgnoreAOP 标记的方法在 AOP 过程中会被忽略
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface IgnoreAOP {
}


