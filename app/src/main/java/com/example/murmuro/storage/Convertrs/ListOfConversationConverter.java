package com.example.murmuro.storage.Convertrs;

import androidx.room.TypeConverter;

import com.example.murmuro.model.Conversation;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

public class ListOfConversationConverter {
    @TypeConverter
    public static HashMap<String, Conversation> fromString(String value) {
        Type listType = new TypeToken<HashMap<String, Conversation>>() {
        }.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromMString(HashMap<String, Conversation> Conversation) {
        Gson gson = new Gson();
        String json = gson.toJson(Conversation);
        return json;
    }
}
