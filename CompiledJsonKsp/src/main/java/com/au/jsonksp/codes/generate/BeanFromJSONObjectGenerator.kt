package com.au.jsonksp.codes.generate

import com.au.jsonannotations.JSONObjectGetType
import com.au.jsonksp.Globals
import com.au.jsonksp.JSONObjectOptGetFuncToJavaBaseType
import com.au.jsonksp.JsonKspProvider
import com.au.jsonksp.codes.AbsGenerator
import com.au.jsonksp.infos.ArrayFieldInfoGetType
import com.au.jsonksp.infos.KtBooleanArrayFieldInfoGetType
import com.au.jsonksp.infos.KtDoubleArrayFieldInfoGetType
import com.au.jsonksp.infos.KtIntArrayFieldInfoGetType
import com.au.jsonksp.infos.KtLongArrayFieldInfoGetType
import com.au.jsonksp.infos.ObjectFieldInfoGetType
import com.au.jsonksp.isGenericClassBaseType
import com.au.jsonksp.kotlinBaseTypeToJSONObjectGetFunc
import com.au.jsonksp.kotlinBaseTypeToJavaGenericType
import com.au.jsonksp.qualifiedNameToCompiledName

/**
 * @author allan
 * @date :2024/7/9 14:46
 * @description:
 */
class BeanFromJSONObjectGenerator(fullClazz:String) : AbsGenerator(fullClazz) {
    private val tempCode = """

        @NonNull
        public static $fullClazz fromJSONObject(JSONObject jo) throws JSONException {
            //todoFromJSONObjectBody
        }
        
        @NonNull
        public static List<$fullClazz> fromJSONArray(JSONArray ja) throws JSONException {
            List<$fullClazz> list = new ArrayList<>();
            //todoFromJSONArrayBody
            return list;
        }

        @Nullable
        public static $fullClazz fromJson(@NonNull String json) throws JSONException {
            return fromJSONObject(new JSONObject(json));
        }
    
        @Nullable
        public static List<$fullClazz> fromJsonList(@NonNull String json) throws JSONException {
            return fromJSONArray(new JSONArray(json));
        }

    """.trimMargin()

    override fun generate() : String {
        val body = StringBuilder()


        val fiList = Globals.class2FieldInfos[fullClazz] ?: return tempCode

        for (fi in fiList) {
            if (!fi.deserialize) continue

            val fromFieldName = if(fi.altName.isNullOrEmpty()) fi.fieldName else fi.altName
            JsonKspProvider.log("generate $fromFieldName ${fi.type.getType}====")
            when (fi.type.getType) {
                JSONObjectGetType.optInt,
                JSONObjectGetType.optBoolean,
                JSONObjectGetType.optDouble,
                JSONObjectGetType.optLong,
                JSONObjectGetType.optString -> {
                    val baseType = JSONObjectOptGetFuncToJavaBaseType(fi.type.getType.toString())
                    body.append(
                        """

            $baseType ${fi.fieldName} = jo.${fi.type.getType}("$fromFieldName");
                    """.trimMargin()
                    ).appendLine()
                }

                JSONObjectGetType.optJSONObject -> {
                    val objectType = fi.type as ObjectFieldInfoGetType
                    val compiledClass = qualifiedNameToCompiledName(objectType.clazz)
                    body.append("""

            JSONObject ${fi.fieldName} = ${compiledClass}.fromJSONObject(jo.getJSONObject("$fromFieldName"));
                    """.trimMargin()).appendLine()
                }
                JSONObjectGetType.optJSONArray -> {
                    val fieldJSONArray = "${fi.fieldName}JSONArray"
                    val jaLen = "${fieldJSONArray}Len"
                    val createList = fi.fieldName

                    if (fi.type is KtIntArrayFieldInfoGetType) {
                        body.append("""

            JSONArray $fieldJSONArray = jo.getJSONArray("$fromFieldName");
            int $jaLen = $fieldJSONArray.length();
            int[] $createList = new int[$jaLen];
            for (int i = 0; i < $jaLen; i++) {
                //todo jsonArray inner is also json Array, List<List<SubClass>>
                $createList[i] = $fieldJSONArray.getInt(i);
            }
            bean.${fi.fieldName} = $createList;
                            """.trimMargin()).appendLine()
                    }  else if (fi.type is KtDoubleArrayFieldInfoGetType) {
                        body.append("""

            JSONArray $fieldJSONArray = jo.getJSONArray("$fromFieldName");
            int $jaLen = $fieldJSONArray.length();
            double[] $createList = new double[$jaLen];
            for (int i = 0; i < $jaLen; i++) {
                //todo jsonArray inner is also json Array, List<List<SubClass>>
                $createList[i] = $fieldJSONArray.getDouble(i);
            }
            bean.${fi.fieldName} = $createList;
                            """.trimMargin()).appendLine()
                    } else if (fi.type is KtLongArrayFieldInfoGetType) {
                        body.append("""

            JSONArray $fieldJSONArray = jo.getJSONArray("$fromFieldName");
            int $jaLen = $fieldJSONArray.length();
            long[] $createList = new long[$jaLen];
            for (int i = 0; i < $jaLen; i++) {
                //todo jsonArray inner is also json Array, List<List<SubClass>>
                $createList[i] = $fieldJSONArray.getLong(i);
            }
            bean.${fi.fieldName} = $createList;
                            """.trimMargin()).appendLine()
                    }  else if (fi.type is KtBooleanArrayFieldInfoGetType) {
                        body.append("""

            JSONArray $fieldJSONArray = jo.getJSONArray("$fromFieldName");
            int $jaLen = $fieldJSONArray.length();
            boolean[] $createList = new boolean[$jaLen];
            for (int i = 0; i < $jaLen; i++) {
                //todo jsonArray inner is also json Array, List<List<SubClass>>
                $createList[i] = $fieldJSONArray.getBoolean(i);
            }
            bean.${fi.fieldName} = $createList;
                            """.trimMargin()).appendLine()
                    } else {
                        fi.type as ArrayFieldInfoGetType
                        val genericClass = fi.type.genericClass
                        val arrayOrList = fi.type.arrayOrList

                        if (isGenericClassBaseType(genericClass)) {
                            val javaGenericClass = kotlinBaseTypeToJavaGenericType(genericClass)
                            val getFunc = kotlinBaseTypeToJSONObjectGetFunc(genericClass)
                            body.append("""

            ArrayList<$javaGenericClass> $createList = new ArrayList<>();
            JSONArray $fieldJSONArray = jo.getJSONArray("$fromFieldName");
            int $jaLen = $fieldJSONArray.length();
            for (int i = 0; i < $jaLen; i++) {
                $createList.add($fieldJSONArray.${getFunc}(i));
            }
            bean.${fi.fieldName} = $createList;
                            """.trimMargin()).appendLine()
                        } else {
                            if (arrayOrList) {
                                body.append("""

            JSONArray $fieldJSONArray = jo.getJSONArray("$fromFieldName");
            int $jaLen = $fieldJSONArray.length();
            $genericClass[] $createList = new $genericClass[$jaLen];
            for (int i = 0; i < $jaLen; i++) {
                //todo jsonArray inner is also json Array, List<List<SubClass>>
                $createList[i] = ${qualifiedNameToCompiledName(genericClass)}.fromJSONObject($fieldJSONArray.getJSONObject(i));
            }
            bean.${fi.fieldName} = $createList;
                            """.trimMargin()).appendLine()
                            } else {
                                body.append("""

            ArrayList<$genericClass> $createList = new ArrayList<>();
            JSONArray $fieldJSONArray = jo.getJSONArray("$fromFieldName");
            int $jaLen = $fieldJSONArray.length();
            for (int i = 0; i < $jaLen; i++) {
                //todo jsonArray inner is also json Array, List<List<SubClass>>
                $createList.add(${qualifiedNameToCompiledName(genericClass)}.fromJSONObject($fieldJSONArray.getJSONObject(i)));
            }
            bean.${fi.fieldName} = $createList;
                            """.trimMargin()).appendLine()
                            }
                        }
                    }
                }
            }
        }

        body.append("""

            $fullClazz bean = new $fullClazz();
        """.trimMargin()).appendLine()


        body.append("""

            return bean;
        """.trimMargin())

        val fromJSONArray = """

            int len = ja.length();
            for (int i = 0; i < len; i++) {
                list.add(fromJSONObject(ja.getJSONObject(i)));
            }
        """.trimMargin()

        return tempCode
            .replace("//todoFromJSONObjectBody", body.toString())
            .replace("//todoFromJSONArrayBody", fromJSONArray + "\n")
    }

}