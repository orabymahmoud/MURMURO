package com.example.murmuro.storage.Convertrs;

import androidx.room.TypeConverter;

import com.example.murmuro.model.Message;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

public class ListOfMessageConverter {
    @TypeConverter
    public static HashMap<String, Message> fromString(String value) {
        Type listType = new TypeToken<HashMap<String, Message>>() {
        }.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromMString(HashMap<String, Message> Message) {
        Gson gson = new Gson();
        String json = gson.toJson(Message);
        return json;
    }
}
