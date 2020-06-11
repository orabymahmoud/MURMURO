package com.example.murmuro.storage.room;

import android.content.Context;

import androidx.room.Room;

public class MurmuroDatabaseClient {
    private static final String DATABASE_NAME = "murmuro";
    private Context context;
    private  MurmuroDatabaseClient client;
    private MurmuroDatabase murmuroDatabase;

    public MurmuroDatabaseClient(Context context) {
        this.context = context;
        murmuroDatabase = Room.databaseBuilder(context, MurmuroDatabase.class, DATABASE_NAME).build();
    }

    public synchronized MurmuroDatabaseClient getClient(Context context) {
        if(client == null)
            client = new MurmuroDatabaseClient(context);

        return client;
    }

    public MurmuroDatabase getMurmuroDatabase() {
        return murmuroDatabase;
    }
}
