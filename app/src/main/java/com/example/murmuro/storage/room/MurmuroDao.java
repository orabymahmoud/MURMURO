package com.example.murmuro.storage.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.murmuro.model.Call;
import com.example.murmuro.model.Conversation;
import com.example.murmuro.model.Person;
import com.example.murmuro.model.User;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;

@Dao
public interface MurmuroDao {

    @Query("SELECT * FROM user")
    Flowable<List<User>> getUsers();

    @Query("SELECT * FROM user WHERE id = :id ")
    Maybe<User> getUserById(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(User user);

    @Query("DELETE FROM user WHERE id = :id")
    void deleteUserById(String id);

    @Query("DELETE FROM user")
    void deleteAllUsers();

    @Update
    void updateUser(User user);

    //--------------------------------------- #Persons#--------------

    @Query("SELECT * FROM people")
    Flowable<List<Person>> getPersons();

    @Query("SELECT * FROM people WHERE id = :id ")
    Maybe<Person> getPersonById(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPerson(Person person);

    @Query("DELETE FROM people WHERE id = :id")
    void deletePersonById(String id);

    @Query("DELETE FROM people")
    void deleteAllPersons();

    @Update
    void updatePerson(Person person);

    //--------------------------------------- #Conversation#--------------

    @Query("SELECT * FROM conversations")
    Flowable<List<Conversation>> getConversations();

    @Query("SELECT * FROM conversations WHERE id = :id ")
    Maybe<Conversation> getConversationById(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertConversation(Conversation conversation);

    @Query("DELETE FROM conversations WHERE id = :id")
    void deleteConversationById(String id);

    @Query("DELETE FROM conversations")
    void deleteAllConversations();

    @Update
    void updateConversation(Conversation conversation);

    //--------------------------------------- #Calls#--------------

    @Query("SELECT * FROM call")
    Flowable<List<Call>> getCalls();

    @Query("SELECT * FROM call WHERE id = :id ")
    Maybe<Call> getCallById(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCall(Call call);

    @Query("DELETE FROM call WHERE id = :id")
    void deleteCallById(String id);

    @Query("DELETE FROM call")
    void deleteAllCalls();

    @Update
    void updateCall(Call call);

}
