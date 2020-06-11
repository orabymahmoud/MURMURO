package com.example.murmuro.storage.Convertrs;

import androidx.room.TypeConverter;

import com.example.murmuro.model.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class UserConverter {
    @TypeConverter
    public static User fromString(String value) {
        Type listType = new TypeToken<List<User>>() {
        }.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromMString(User User) {
        Gson gson = new Gson();
        String json = gson.toJson(User);
        return json;
    }
}
