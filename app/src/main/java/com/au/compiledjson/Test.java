package com.au.compiledjson;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.au.compiledjson.bean.JavaPersonBean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * @author allan
 * @date :2024/7/10 9:26
 * @description:
 */
public class Test {

    @Nullable
    public static JavaPersonBean fromJson(@NonNull String json) throws JSONException {
        JSONObject jo = new JSONObject(json);
        return JavaPersonBeanCompiled.fromJSONObject(jo);
    }

    @Nullable
    public static List<JavaPersonBean> fromJsonList(@NonNull String json) throws JSONException {
        JSONArray jo = new JSONArray(json);
        return JavaPersonBeanCompiled.fromJSONArray(ja);
    }
}
