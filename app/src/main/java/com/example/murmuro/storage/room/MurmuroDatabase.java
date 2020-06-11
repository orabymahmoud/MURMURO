package com.example.murmuro.storage.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.murmuro.model.Call;
import com.example.murmuro.model.Conversation;
import com.example.murmuro.model.Person;
import com.example.murmuro.model.User;
import com.example.murmuro.storage.Convertrs.ListOfConversationConverter;
import com.example.murmuro.storage.Convertrs.ListOfMessageConverter;
import com.example.murmuro.storage.Convertrs.ListOfPersonConverter;
import com.example.murmuro.storage.Convertrs.ListOfUserConverter;
import com.example.murmuro.storage.Convertrs.MessageConverter;
import com.example.murmuro.storage.Convertrs.PersonConverter;
import com.example.murmuro.storage.Convertrs.UnreadMessagesConverter;
import com.example.murmuro.storage.Convertrs.UserConverter;


@Database(entities = {User.class, Conversation.class, Person.class, Call.class} , version = 2, exportSchema = false)
@TypeConverters({ListOfConversationConverter.class, UnreadMessagesConverter.class ,ListOfMessageConverter.class, ListOfUserConverter.class, MessageConverter.class, UserConverter.class, PersonConverter.class, ListOfPersonConverter.class})
public abstract class MurmuroDatabase extends RoomDatabase {
    public abstract MurmuroDao murmuroDao();
}
