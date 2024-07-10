package com.au.jsonannotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个变量。
 * 第一版CompiledJson库，必须每一个字段都打上注解。
 * 后续通过apt代码实现自动解析没有打上标记，并移除Transient的字段。
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface CompiledJsonField {
    /**
     * 允许该字段序列化
     */
    boolean serialize() default true;

    /**
     * 允许该字段反序列
     */
    boolean deserialize() default true;
    /**
     * 后台的字段名。比如后台返回的是class，端代码写的是clazz。
     */
    String altName() default "";
}