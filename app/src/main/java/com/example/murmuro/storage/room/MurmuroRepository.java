package com.example.murmuro.storage.room;

import com.example.murmuro.model.Call;
import com.example.murmuro.model.Conversation;
import com.example.murmuro.model.Person;
import com.example.murmuro.model.User;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;

public interface MurmuroRepository {

    public Flowable<List<User>> getUsers();
    public Maybe<User> getUserById(String id);
    public void insertUSer(User user);
    public void deleteAllUsers();
    public void deleteUserById(String id);
    public void updateUser(User user);

    //--------------------------------#Peoples#-----------------

    public Flowable<List<Person>> getPersons();

    public Maybe<Person> getPersonById(String id);

    public void insertPerson(Person person);

    public void deletePersonById(String id);

    public void deleteAllPersons();

    public void updatePerson(Person person);

    //--------------------------------------- #Conversation#--------------

    public Flowable<List<Conversation>> getConversations();

    public Maybe<Conversation> getConversationById(String id);

    public void insertConversation(Conversation conversation);

    public void deleteConversationById(String id);

    public void deleteAllConversations();

    public void updateConversation(Conversation conversation);

    //--------------------------------------- #Calls#--------------

    Flowable<List<Call>> getCalls();

    Maybe<Call> getCallById(String id);

    void insertCall(Call call);

    void deleteCallById(String id);

    void deleteAllCalls();

    void updateCall(Call call);

}
