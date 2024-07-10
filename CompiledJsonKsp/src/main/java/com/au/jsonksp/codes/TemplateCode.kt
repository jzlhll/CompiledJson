package com.au.jsonksp.codes

/**
 * @author allan
 * @date :2024/7/8 11:42
 * @description:
 */
class TemplateCode(isInner:Boolean) {
    fun insertPackage(pkg:String) {
        code = code.replace("%package%", pkg)
    }

    fun insertBeanSimpleName(classQualifiedName:String) {
        code = code.replace("%name%", classQualifiedName)
    }

    fun insertBody(body:String) {
        code = code.replace("//todoBody", body)
    }

    var code = if(!isInner) """
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
    """.trimIndent()
    else
        """
            public final static class %name%Compiled {
                //todoBody
            }

        """.trimIndent()
    private set
}