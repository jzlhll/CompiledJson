package com.au.jsonksp.codes

abstract class TemplateCode {
    abstract fun insertBeanSimpleName(classQualifiedName:String)
    abstract fun insertBody(body:String)
    abstract val endCode:String
}

class TopTemplateCode(private val extension:Extension = Extension.Java) : TemplateCode() {
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

    private var code = initCode()

    private fun initCode() : String {
        return when (extension) {
            Extension.Kotlin -> {
"""
package %package%

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class %name%Compiled {
    companion object {
    //todoBody
    }
}
    """.trimMargin()
            }

            Extension.Java -> {
"""
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
        }
    }
}

class InnerTemplateCode(private val extension: Extension = Extension.Java) : TemplateCode(){
    override fun insertBeanSimpleName(classQualifiedName:String) {
        code = code.replace("%name%", classQualifiedName)
    }

    override fun insertBody(body:String) {
        code = code.replace("//todoBody", body)
    }

    override val endCode: String
        get() = code

    private var code = initCode()

    private fun initCode():String{
        return when(extension) {
            Extension.Kotlin -> {
                """

    class %name%Compiled {
        companion object {
        //todoBody
        }
    }

        """.trimMargin()
            }
            Extension.Java -> {
                """

    public static final class %name%Compiled {
        //todoBody
    }

        """.trimMargin()
            }
        }
    }
}