package com.au.jsonksp.codes

abstract class TemplateCode {
    abstract fun insertBeanSimpleName(classQualifiedName:String)
    abstract fun insertBody(body:String)
    abstract val endCode:String
}

class TopTemplateCode : TemplateCode() {
    fun insertPackage(pkg:String) {
        code = code.replace("%package%", pkg)
    }

    override fun insertBeanSimpleName(classQualifiedName:String) {
        code = code.replace("%name%", classQualifiedName)
    }

    override fun insertBody(body:String) {
        code = code.replace("//todoBody", body)
    }

    override val endCode: String
        get() = code

    private var code = """
package %package%;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.*;

public final class %name%Compiled {
    //todoBody
}
    """.trimMargin()
}

class InnerTemplateCode : TemplateCode(){
    override fun insertBeanSimpleName(classQualifiedName:String) {
        code = code.replace("%name%", classQualifiedName)
    }

    override fun insertBody(body:String) {
        code = code.replace("//todoBody", body)
    }

    override val endCode: String
        get() = code

    private var code = """

    public static final class %name%Compiled {
        //todoBody
    }

        """.trimMargin()
}