package com.au.jsonksp.codes

import com.au.jsonannotations.JSONObjectGetType
import com.au.jsonksp.Globals
import com.au.jsonksp.infos.ArrayFieldInfoGetType
import com.au.jsonksp.infos.ObjectFieldInfoGetType
import com.au.jsonksp.isGenericClassBaseType
import com.au.jsonksp.qualifiedNameToCompiledName

/**
 * @author allan
 * @date :2024/7/8 15:17
 * @description:
 */
class BeanToJSONObjectGenerator(private val fullClazz:String) {
    private val compiledFullClazz = qualifiedNameToCompiledName(fullClazz)

    private val tempCode = """
        @NonNull
        public static JSONObject toJSONObject(@NonNull $fullClazz bean) {
        JSONObject jo = new JSONObject();
        //todoJsonObjectBody
        return jo;
        }
        
        @NonNull
        public static JSONArray toJSONArray(@NonNull $fullClazz[] beans) {
        //todoJsonArrayBody
        }

        @NonNull
        public static JSONArray toJSONArray(@NonNull List<$fullClazz> beans) {
        //todoJsonArrayBody
        }
    """.trimIndent()

    fun generate() : String {
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
                    """.trimIndent())
                }
                JSONObjectGetType.getJSONObject ->
                {
                    val objectType = fi.type as ObjectFieldInfoGetType
                    val compiledClass = qualifiedNameToCompiledName(objectType.clazz)
                    body.append("""
                        jo.put("$outFieldName", "${compiledClass}.toJSONObject(${beanField})");
                    """.trimIndent())
                }
                JSONObjectGetType.getJSONArray -> {
                    val arrayType = fi.type as ArrayFieldInfoGetType
                    val genericClass = arrayType.genericClass

                    if (isGenericClassBaseType(genericClass)) {
                        val item = "${fi.fieldName}Item"
                        val jsonArr = "${fi.fieldName}JsonArr"
                        body.append("""
                        JSONArray $jsonArr = new JSONArray();
                        for (${genericClass} $item : $beanField) {
                            $jsonArr.put($item);
                        }
                        jo.put("$outFieldName", $jsonArr);

                        """.trimIndent())
                    } else {
                        val compiledClass = qualifiedNameToCompiledName(genericClass)
                        body.append("""
                        jo.put("$outFieldName", ${compiledClass}.toJSONArray($beanField));
                        """.trimIndent())
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
        """.trimIndent()

        return tempCode
            .replace("//todoJsonArrayBody", jsonArrayBody)
            .replace("//todoJsonObjectBody", body.toString())
    }
}