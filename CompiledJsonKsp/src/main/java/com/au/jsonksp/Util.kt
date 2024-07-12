package com.au.jsonksp

import com.au.jsonannotations.JSONObjectGetType
import com.au.jsonksp.JsonKspProvider.Companion.env
import com.au.jsonksp.JsonKspProvider.Companion.log
import com.au.jsonksp.infos.ArrayFieldInfoGetType
import com.au.jsonksp.infos.FieldInfoGetType
import com.au.jsonksp.infos.ObjectFieldInfoGetType
import com.au.jsonksp.infos.KtBooleanArrayFieldInfoGetType
import com.au.jsonksp.infos.KtDoubleArrayFieldInfoGetType
import com.au.jsonksp.infos.KtIntArrayFieldInfoGetType
import com.au.jsonksp.infos.KtLongArrayFieldInfoGetType
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSTypeReference
import java.io.OutputStreamWriter


fun writeToFile(packageName:String, fileName:String, code:String) {
    // 生成文件
    val file = env.codeGenerator.createNewFile(
        dependencies = Dependencies(false),
        packageName = packageName,
        fileName = fileName,
        extensionName = "java"
    )

    // 写入文件内容
    OutputStreamWriter(file).use { writer ->
        writer.write(code)
    }

}

fun isGenericClassBaseType(genericClass:String) : Boolean{
    return (genericClass == "kotlin.Int"
            || genericClass == "kotlin.String"
            || genericClass == "kotlin.Double"
            || genericClass == "kotlin.Long"
            || genericClass == "kotlin.Boolean"
            || genericClass == "int"
            || genericClass == "long"
            || genericClass == "double"
            || genericClass == "boolean"
            || genericClass == "String")
}

/**
 * 比如kotlin中的List<Int> 转成java的List<Integer>
 */
fun kotlinBaseTypeToJavaGenericType(type:String) : String {
    return when (type) {
        "kotlin.Int" -> "Integer"
        "kotlin.String" -> "String"
        "kotlin.Double" -> "Double"
        "kotlin.Long" -> "Long"
        "kotlin.Boolean" -> "Boolean"
        else -> type
    }
}

fun kotlinBaseTypeToJSONObjectGetFunc(type:String) : String {
    return when (type) {
        "kotlin.Int" -> "getInt"
        "kotlin.String" -> "getString"
        "kotlin.Double" -> "getDouble"
        "kotlin.Long" -> "getLong"
        "kotlin.Boolean" -> "getBoolean"
        else -> ""
    }
}

@Throws
fun KSTypeReference.toCompiledJsonType(log:String) : FieldInfoGetType {
    val toStr = toString()
    return when (toStr) {
        "Boolean", "boolean", "bool" -> FieldInfoGetType(JSONObjectGetType.getBoolean)
        "double", "Double" -> FieldInfoGetType(JSONObjectGetType.getDouble)
        "int", "Integer", "Int" -> FieldInfoGetType(JSONObjectGetType.getInt)
        "long", "Long" -> FieldInfoGetType(JSONObjectGetType.getLong)
        "String" -> FieldInfoGetType(JSONObjectGetType.getString)
        "IntArray" -> KtIntArrayFieldInfoGetType(JSONObjectGetType.getJSONArray)
        "LongArray" -> KtLongArrayFieldInfoGetType(JSONObjectGetType.getJSONArray)
        "DoubleArray" -> KtDoubleArrayFieldInfoGetType(JSONObjectGetType.getJSONArray)
        "BooleanArray" -> KtBooleanArrayFieldInfoGetType(JSONObjectGetType.getJSONArray)
        else ->
        {
            //带泛型SubBean的List形式
            val class2GenericClass = parseList(this)
            val outClass = class2GenericClass.first
            val genericClass = class2GenericClass.second
            //todo 不论是List泛型，还是直接的subBean。都需要检验是否是一个CompiledJsonBean

            val isList = toStr.contains("List<")
            val isArray = toStr.contains("Array<")
            if (isList || isArray) {
                log("is list class2GenericClass: $class2GenericClass")
                if (outClass != null && genericClass != null) {
                    ArrayFieldInfoGetType(JSONObjectGetType.getJSONArray, genericClass, isArray).also {
//                        if (isGenericClassBaseType(genericClass)) {
//                            it.checked = true
//                        }
                    }
                } else {
                    throw IllegalArgumentException("[CompiledJson] Please change $log to known type\n java is " +
                            "{boolean, double, int, long, String, List<XXClass>, XXClass[],} or Wrap types(such as Integer), kotlin is " +
                            "{Boolean, Double, Int, Long, String, List<XXClass>, Array<XXClass>,}, ")
                }
            } else {
                if (outClass != null && genericClass != null) {
                    //不支持的泛型。
                    throw IllegalArgumentException("[CompiledJson] Please change $log to known type\n java is " +
                            "{boolean, double, int, long, String, List<XXClass>, XXClass[],} or Wrap types(such as Integer), kotlin is " +
                            "{Boolean, Double, Int, Long, String, List<XXClass>, Array<XXClass>,}, ")
                } else if (outClass != null) {
                    ObjectFieldInfoGetType(JSONObjectGetType.getJSONObject, outClass)
                } else {
                    throw IllegalArgumentException("[CompiledJson] Please change $log to known type\n java is " +
                            "{boolean, double, int, long, String, List<XXClass>, XXClass[],} or Wrap types(such as Integer), kotlin is " +
                            "{Boolean, Double, Int, Long, String, List<XXClass>, Array<XXClass>,}, ")
                }
            }
        }
    }
}

private fun parseList(t : KSTypeReference) : Pair<String?, String?> {
    val type = t.resolve()
    val genericArguments = type.arguments
    val clazz = type.declaration.qualifiedName?.asString()
    val genericClass = genericArguments.joinToString { it.type?.resolve()?.declaration?.qualifiedName?.asString() ?: "" }
    val fixGenericClass = if(genericClass == "") null else genericClass
    return clazz to fixGenericClass
}

fun qualifiedNameToPkgAndParent(fullName:String) : Pair<String, String?> {
    val items = fullName.split(".")
    val len = items.size
    var i = len - 2
    var level = 0
    while (i > 0) {
        if (items[i].lowercase() == items[i]) {
            break
        }
        level++
        i--
    }

    val pkgSb = StringBuilder()
    do {
        var j = 0
        while (j <= i) {
            pkgSb.append(items[j]).append(".")
            j++
        }
        pkgSb.deleteAt(pkgSb.length - 1)
    } while(false)

    val pkg = pkgSb.toString()

    if (level == 0) {
        return pkg to null
    } else {
        val parentSb = StringBuilder()
        var j = 0
        while (j < len - 1) {
            parentSb.append(items[j]).append(".")
            j++
        }
        parentSb.deleteAt(parentSb.length - 1)

        return pkg to parentSb.toString()
    }
}

fun qualifiedNameToCompiledName(fullName:String) : String {
    val items = fullName.split(".")
    val len = items.size
    val sb = StringBuilder()
    var i = 0
    while (i < len) {
        if (items[i].lowercase() != items[i]) {
            sb.append(items[i] + "Compiled").append(".")
        }
        i++
    }
    sb.deleteAt(sb.length - 1)
    return sb.toString()
}