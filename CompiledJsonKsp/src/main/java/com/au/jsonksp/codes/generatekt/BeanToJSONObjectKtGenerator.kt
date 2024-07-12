package com.au.jsonksp.codes.generatekt

import com.au.jsonannotations.JSONObjectGetType
import com.au.jsonksp.Globals
import com.au.jsonksp.codes.AbsGenerator
import com.au.jsonksp.infos.ArrayFieldInfoGetType
import com.au.jsonksp.infos.KtBooleanArrayFieldInfoGetType
import com.au.jsonksp.infos.KtDoubleArrayFieldInfoGetType
import com.au.jsonksp.infos.KtIntArrayFieldInfoGetType
import com.au.jsonksp.infos.KtLongArrayFieldInfoGetType
import com.au.jsonksp.infos.ObjectFieldInfoGetType
import com.au.jsonksp.isGenericClassBaseType
import com.au.jsonksp.kotlinBaseTypeToJavaGenericType
import com.au.jsonksp.qualifiedNameToCompiledName

/**
 * @author allan
 * @date :2024/7/8 15:17
 * @description:
 */
class BeanToJSONObjectKtGenerator(fullClazz:String) : AbsGenerator(fullClazz) {
    private val tempCode = """

        @Throws(JSONException::class)
        fun toJSONObject(bean:$fullClazz) : JSONObject {
            val jo = JSONObject()
            //todoJsonObjectBody
            return jo
        }

        @Throws(JSONException::class)
        fun toJSONArray(beans:Array<$fullClazz>) : JSONArray {
            //todoJsonArrayBody
        }

        @Throws(JSONException::class)
        fun toJSONArray(beans:List<$fullClazz>) : JSONArray {
            //todoJsonArrayBody
        }

    """.trimMargin()

    override fun generate() : String {
        val fiList = Globals.class2FieldInfos[fullClazz] ?: return tempCode

        val body = StringBuilder()
        for (fi in fiList) {
            if (!fi.serialize) continue

            val outFieldName = if(fi.altName.isNullOrEmpty()) fi.fieldName else fi.altName
            val beanField = "bean.${fi.fieldName}"
            when (fi.type.getType) {
                JSONObjectGetType.getInt,
                JSONObjectGetType.getBoolean,
                JSONObjectGetType.getDouble,
                JSONObjectGetType.getLong,
                JSONObjectGetType.getString -> {
                    body.append("""

            jo.put("$outFieldName", $beanField)
                    """.trimMargin()).appendLine()
                }
                JSONObjectGetType.getJSONObject ->
                {
                    val objectType = fi.type as ObjectFieldInfoGetType
                    val compiledClass = qualifiedNameToCompiledName(objectType.clazz)
                    body.append("""

            jo.put("$outFieldName", ${compiledClass}.toJSONObject(${beanField}))
                    """.trimMargin()).appendLine()
                }
                JSONObjectGetType.getJSONArray -> {
                    val genericClass = (fi.type as ArrayFieldInfoGetType).genericClass

                    if (isGenericClassBaseType(genericClass)) {
                        val item = "${fi.fieldName}Item"
                        val jsonArr = "${fi.fieldName}JsonArr"
                        body.append("""

            val $jsonArr = JSONArray()
            for ($item in $beanField) {
                $jsonArr.put($item)
            }
            jo.put("$outFieldName", $jsonArr)
                        """.trimMargin()).appendLine()
                    } else {
                        val compiledClass = qualifiedNameToCompiledName(genericClass)
                        body.append("""

            jo.put("$outFieldName", ${compiledClass}.toJSONArray($beanField))
                        """.trimMargin()).appendLine()
                    }
                }
            }
        }

        val jsonArrayBody = """

            val ja = JSONArray()
            for (bean in beans) {
                ja.put(toJSONObject(bean))
            }
            return ja
        """.trimMargin()

        return tempCode
            .replace("//todoJsonArrayBody", jsonArrayBody + "\n")
            .replace("//todoJsonObjectBody", body.toString())
    }
}