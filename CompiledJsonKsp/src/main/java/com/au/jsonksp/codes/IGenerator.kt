package com.au.jsonksp.codes

/**
 * @author allan
 * @date :2024/7/12 10:13
 * @description:
 */
abstract class AbsGenerator(protected val fullClazz:String) {
    abstract fun generate():String
}