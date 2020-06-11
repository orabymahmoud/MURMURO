package com.example.murmuro.storage.Convertrs;

import androidx.room.TypeConverter;

import com.example.murmuro.model.Person;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class ListOfPersonConverter {
    @TypeConverter
    public static List<Person> fromString(String value) {
        Type listType = new TypeToken<List<Person>>() {
        }.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromMString(List<Person> Person) {
        Gson gson = new Gson();
        String json = gson.toJson(Person);
        return json;
    }
}
