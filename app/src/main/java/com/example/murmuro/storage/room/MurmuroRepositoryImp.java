package com.example.murmuro.storage.room;

import androidx.annotation.NonNull;

import com.example.murmuro.model.Call;
import com.example.murmuro.model.Conversation;
import com.example.murmuro.model.Person;
import com.example.murmuro.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.Maybe;

public class MurmuroRepositoryImp implements MurmuroRepository{

    private MurmuroDao murmuroDao;
    private Executor executor;

    @Inject
    public MurmuroRepositoryImp(MurmuroDao murmuroDao, Executor executor) {
        this.murmuroDao = murmuroDao;
        this.executor = executor;
    }

    @Override
    public Flowable<List<User>> getUsers() {
        return murmuroDao.getUsers();
    }

    @Override
    public Maybe<User> getUserById(String id) {
        return murmuroDao.getUserById(id);
    }

    @Override
    public void insertUSer(final User user) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                murmuroDao.insertUser(user);
            }
        });
    }

    @Override
    public void deleteAllUsers() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                murmuroDao.deleteAllUsers();
            }
        });
    }

    @Override
    public void deleteUserById(final String id) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                murmuroDao.deleteUserById(id);
            }
        });
    }

    @Override
    public void updateUser(final User user) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                murmuroDao.updateUser(user);
            }
        });
    }

    @Override
    public Flowable<List<Person>> getPersons() {
        return murmuroDao.getPersons();
    }

    @Override
    public Maybe<Person> getPersonById(String id) {
        return murmuroDao.getPersonById(id);
    }

    @Override
    public void insertPerson(final Person person) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                murmuroDao.insertPerson(person);
            }
        });
    }

    @Override
    public void deletePersonById(final String id) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                murmuroDao.deletePersonById(id);
            }
        });
    }

    @Override
    public void deleteAllPersons() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                murmuroDao.deleteAllPersons();
            }
        });
    }

    @Override
    public void updatePerson(final Person person) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                murmuroDao.updatePerson(person);
            }
        });
    }

    @Override
    public Flowable<List<Conversation>> getConversations() {
        return murmuroDao.getConversations();
    }

    @Override
    public Maybe<Conversation> getConversationById(String id) {
        return murmuroDao.getConversationById(id);
    }

    @Override
    public void insertConversation(final Conversation conversation) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                murmuroDao.insertConversation(conversation);
            }
        });
    }

    @Override
    public void deleteConversationById(final String id) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                murmuroDao.deleteConversationById(id);
            }
        });
    }

    @Override
    public void deleteAllConversations() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                murmuroDao.deleteAllConversations();
            }
        });
    }

    @Override
    public void updateConversation(final Conversation conversation) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                murmuroDao.updateConversation(conversation);
            }
        });
    }

    @Override
    public Flowable<List<Call>> getCalls() {
        return murmuroDao.getCalls();
    }

    @Override
    public Maybe<Call> getCallById(String id) {
        return murmuroDao.getCallById(id);
    }

    @Override
    public void insertCall(final Call call) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                murmuroDao.insertCall(call);
            }
        });
    }

    @Override
    public void deleteCallById(final String id) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                murmuroDao.deleteCallById(id);
            }
        });
    }

    @Override
    public void deleteAllCalls() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                murmuroDao.deleteAllCalls();
            }
        });
    }

    @Override
    public void updateCall(final Call call) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                murmuroDao.updateCall(call);
            }
        });
    }
}
