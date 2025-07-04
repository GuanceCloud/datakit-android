package com.ft.sdk.garble.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation tag, used for ft-plugin automatic collection to mark the difference, and methods marked with IgnoreAOP will be ignored during AOP
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface IgnoreAOP {
}


