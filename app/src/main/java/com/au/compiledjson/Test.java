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
}
