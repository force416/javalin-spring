package org.eric.javalinspring.util;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class JSONUtil {
    public static boolean isJSON(String json) {
        Gson gson = new Gson();
        try {
            gson.fromJson(json, Object.class);
        } catch (JsonSyntaxException ex) {
            return false;
        }

        return true;
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        return new Gson().fromJson(json, classOfT);
    }
}
