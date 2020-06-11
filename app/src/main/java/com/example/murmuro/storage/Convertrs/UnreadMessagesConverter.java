package com.example.murmuro.storage.Convertrs;

import androidx.room.TypeConverter;

import com.example.murmuro.model.Conversation;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;

public class UnreadMessagesConverter {
    @TypeConverter
    public static HashMap<String, Object> fromString(String value) {
        Type listType = new TypeToken<HashMap<String, Object>>() {
        }.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromMString(HashMap<String, Object> Conversation) {
        Gson gson = new Gson();
        String json = gson.toJson(Conversation);
        return json;
    }
}
