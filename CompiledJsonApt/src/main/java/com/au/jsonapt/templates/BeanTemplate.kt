package com.au.jsonapt.templates

class BeanTemplate {
    companion object {
        private const val MAX_CLASS_NAME_ORIG = 64

        private const val SUFFIX = "Compiled"
        private const val MAX_CLASS_NAME_LEN = MAX_CLASS_NAME_ORIG - SUFFIX.length

        fun getClassName(className:String) : String{
            if (className.length > MAX_CLASS_NAME_LEN) {
                throw RuntimeException("Please change your bean $className's length less than $MAX_CLASS_NAME_LEN.")
            }
            return className + SUFFIX
        }
    }

    val template = """
package //todo packageName;


import androidx.annotation.NonNull;

import com.au.jsonannotations.ICompiled;
import com.au.jsonannotations.Type;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Map;

public class PersonBeanCompiled implements ICompiled<%Bean%> {

    @NonNull
    @Override
    public String toJson(@NonNull %Bean% personBean) {
        //todo toJson
    }

    @Override
    public %Bean% fromJson(@NonNull String jsonStr)  throws JSONException{
        //todo fromJson
    }

    @Override
    public Map<String, Type> serialMap() {
        //todo serialMap
    }

    @Override
    public %Bean% fromJSONObject(@NonNull JSONObject jo) throws JSONException {
        //todo fromJSONObject
    }
}

    """.trimIndent()

}