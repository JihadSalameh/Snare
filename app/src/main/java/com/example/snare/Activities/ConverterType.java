package com.example.snare.Activities;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class ConverterType {
    @TypeConverter
    public static String fromList(List<String> myList) {
        Gson gson = new Gson();
        return gson.toJson(myList);
    }

    @TypeConverter
    public static List<String> toList(String myListString) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(myListString, type);
    }
}
