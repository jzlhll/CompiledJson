package com.au.jsonksp.codes.generate

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
class BeanToJSONObjectGenerator(fullClazz:String) : AbsGenerator(fullClazz){
    private val tempCode = """
        @NonNull
        public static JSONObject toJSONObject(@NonNull $fullClazz bean) throws JSONException {
            JSONObject jo = new JSONObject();
            //todoJsonObjectBody
            return jo;
        }
        
        @NonNull
        public static JSONArray toJSONArray(@NonNull $fullClazz[] beans) throws JSONException {
            //todoJsonArrayBody
        }

        @NonNull
        public static JSONArray toJSONArray(@NonNull List<$fullClazz> beans) throws JSONException {
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

            jo.put("$outFieldName", $beanField);
                    """.trimMargin()).appendLine()
                }
                JSONObjectGetType.getJSONObject ->
                {
                    val objectType = fi.type as ObjectFieldInfoGetType
                    val compiledClass = qualifiedNameToCompiledName(objectType.clazz)
                    body.append("""

            jo.put("$outFieldName", ${compiledClass}.toJSONObject(${beanField}));
                    """.trimMargin()).appendLine()
                }
                JSONObjectGetType.getJSONArray -> {
                    val genericClass = when (fi.type) {
                        is KtIntArrayFieldInfoGetType -> {
                            "int"
                        }

                        is KtLongArrayFieldInfoGetType -> {
                            "long"
                        }

                        is KtBooleanArrayFieldInfoGetType -> {
                            "boolean"
                        }

                        is KtDoubleArrayFieldInfoGetType -> {
                            "double"
                        }

                        else -> {
                            (fi.type as ArrayFieldInfoGetType).genericClass
                        }
                    }

                    if (isGenericClassBaseType(genericClass)) {
                        val item = "${fi.fieldName}Item"
                        val jsonArr = "${fi.fieldName}JsonArr"
                        val javaGenericClass = kotlinBaseTypeToJavaGenericType(genericClass)
                        body.append("""

            JSONArray $jsonArr = new JSONArray();
            for (${javaGenericClass} $item : $beanField) {
                $jsonArr.put($item);
            }
            jo.put("$outFieldName", $jsonArr);
                        """.trimMargin()).appendLine()
                    } else {
                        val compiledClass = qualifiedNameToCompiledName(genericClass)
                        body.append("""

            jo.put("$outFieldName", ${compiledClass}.toJSONArray($beanField));
                        """.trimMargin()).appendLine()
                    }
                }
            }
        }

        val jsonArrayBody = """

            JSONArray ja = new JSONArray();
            for ($fullClazz bean : beans) {
                ja.put(toJSONObject(bean));
            }
            return ja;
        """.trimMargin()

        return tempCode
            .replace("//todoJsonArrayBody", jsonArrayBody)
            .replace("//todoJsonObjectBody", body.toString())
    }
}