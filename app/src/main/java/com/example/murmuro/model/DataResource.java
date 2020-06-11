package com.example.murmuro.model;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DataResource<T> {

    @NonNull
    public final Status status;

    @Nullable
    public final T data;

    @Nullable
    public final String message;


    public DataResource(@NonNull Status status, @Nullable T data, @Nullable String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public static <T> DataResource<T> success (@Nullable T data) {
        return new DataResource<>(Status.SUCCESS, data, null);
    }

    public static <T> DataResource<T> error(@NonNull String msg, @Nullable T data) {
        return new DataResource<>(Status.ERROR, data, msg);
    }

    public static <T> DataResource<T> loading(@Nullable T data) {
        return new DataResource<>(Status.LOADING, data, null);
    }

    public enum Status { SUCCESS, ERROR, LOADING}

}

















