package com.au.jsonksp.infos

/**
 * @author allan
 * @date :2024/7/11 9:49
 * @description:
 */
data class ClassTree(val qualifiedName:String, val innerClasses:MutableList<ClassTree>)