package com.au.jsonksp.codes

import com.au.jsonannotations.JSONObjectGetType
import com.au.jsonksp.Globals
import com.au.jsonksp.infos.ArrayFieldInfoGetType
import com.au.jsonksp.infos.ObjectFieldInfoGetType
import com.au.jsonksp.isGenericClassBaseType
import com.au.jsonksp.qualifiedNameToCompiledName

/**
 * @author allan
 * @date :2024/7/9 14:46
 * @description:
 */
class BeanFromJSONObjectGenerator(private val fullClazz:String) {
    private val tempCode = """
        @NonNull
        public static $fullClazz fromJSONObject(JSONObject jo) throws JSONException {
        $fullClazz bean = new $fullClazz();
        //todoFromJSONObjectBody
        return bean;
        }
        
        @NonNull
        public static List<$fullClazz> fromJSONArray(JSONArray ja) throws JSONException {
        List<$fullClazz> list = new ArrayList<>();
        //todoFromJSONArrayBody
        return list;
        }

        @Nullable
        public static $fullClazz fromJson(@NonNull String json) throws JSONException {
            JSONObject jo = new JSONObject(json);
            return fromJSONObject(jo);
        }
    
        @Nullable
        public static List<$fullClazz> fromJsonList(@NonNull String json) throws JSONException {
            JSONArray jo = new JSONArray(json);
            return fromJSONArray(ja);
        }
    """.trimIndent()

    fun generate() : String {
        val body = StringBuilder()
        val fiList = Globals.class2FieldInfos[fullClazz] ?: return tempCode

        for (fi in fiList) {
            if (!fi.deserialize) continue

            val fromFieldName = if(fi.altName.isNullOrEmpty()) fi.fieldName else fi.altName
            when (fi.type.getType) {
                JSONObjectGetType.getInt,
                JSONObjectGetType.getBoolean,
                JSONObjectGetType.getDouble,
                JSONObjectGetType.getLong,
                JSONObjectGetType.getString -> {
                    body.append(
                        """
                            bean.${fi.fieldName} = jo.${fi.type.getType}("$fromFieldName");
                    """.trimIndent()
                    )
                }

                JSONObjectGetType.getJSONObject -> {
                    val objectType = fi.type as ObjectFieldInfoGetType
                    val fieldJo = "${fi.fieldName}Jo"
                    val compiledClass = qualifiedNameToCompiledName(objectType.clazz)
                    body.append("""
                        JSONObject $fieldJo = jo.getJSONObject("$fromFieldName");
                        bean.${fi.fieldName} = ${compiledClass}.fromJSONObject($fieldJo);
                    """.trimIndent())
                }
                JSONObjectGetType.getJSONArray -> {
                    val arrayType = fi.type as ArrayFieldInfoGetType
                    val genericClass = arrayType.genericClass
                    val fieldJa = "${fi.fieldName}Ja"
                    val jaLen = "${fieldJa}Len"
                    val createList = fi.fieldName
                    if (isGenericClassBaseType(genericClass)) {
                        body.append("""
                            JSONArray $fieldJa = jo.getJSONArray("$fromFieldName");
                            int $jaLen = $fieldJa.length();
                            ArrayList<$genericClass> $createList = new ArrayList<>();
                            for (int i = 0; i < $jaLen; i++) {
                                list.add($fieldJa.${fi.type.getType}(i));
                            }
                            bean.${fi.fieldName} = $createList;//todo array[]

                        """.trimIndent())
                    } else {
                        body.append("""
                            JSONArray $fieldJa = jo.getJSONArray("$fromFieldName");
                            int $jaLen = $fieldJa.length();
                            ArrayList<$genericClass> $createList = new ArrayList<>();
                            for (int i = 0; i < $jaLen; i++) {
                                list.add($fieldJa.${fi.type.getType}(i));
                            }
                            bean.${fi.fieldName} = $createList;

                        """.trimIndent())
                    }
                }
            }
        }

        return tempCode
    }
}