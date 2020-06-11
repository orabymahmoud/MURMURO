package com.example.murmuro.ui.main.personprofile;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.murmuro.model.Conversation;
import com.example.murmuro.model.DataResource;
import com.example.murmuro.model.Message;
import com.example.murmuro.model.Person;
import com.example.murmuro.model.User;
import com.example.murmuro.storage.room.MurmuroRepositoryImp;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import io.reactivex.MaybeObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class PersonProfileViewModel extends ViewModel {

    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    private Activity activity;
    private MurmuroRepositoryImp murmuroRepositoryImp;
    private MutableLiveData<DataResource<Conversation>> conversationDataResourceMutableLiveData = new MutableLiveData<>();

    @Inject
    public PersonProfileViewModel(FirebaseDatabase firebaseDatabase, FirebaseAuth firebaseAuth,MurmuroRepositoryImp murmuroRepositoryImp) {
        this.firebaseDatabase = firebaseDatabase;
        this.firebaseAuth = firebaseAuth;
        this.murmuroRepositoryImp = murmuroRepositoryImp;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public  MutableLiveData<DataResource<Conversation>> getConversationId(final Person person)
    {
        final User[] cuurentUser = {null};
        conversationDataResourceMutableLiveData.setValue(DataResource.loading((Conversation) null));

        murmuroRepositoryImp.getUserById(firebaseAuth.getCurrentUser().getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MaybeObserver<User>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.e(TAG, "onSubscribe: " );
                    }

                    @Override
                    public void onSuccess(final User user) {
                        cuurentUser[0] = new User(
                                user.getId(),
                                user.getName(),
                                user.getUsername(),
                                user.getPassword(),
                                user.getPhoto(),
                                user.getEmail(),
                                user.getCity(),
                                user.getMobile(),
                                user.getBio(),
                                user.getStatus(),
                                user.getConversations()
                        );

                        DatabaseReference myRef = firebaseDatabase.
                                getReference("users").
                                child(user.getId()).
                                child("conversations").
                                child(person.getId());

                        myRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot != null)
                                {
                                    Log.e(TAG, "onDataChange: " + dataSnapshot.getValue() );
                                    if(dataSnapshot.getValue() != null)
                                    {
                                        Log.e(TAG, "onDataChange: " + dataSnapshot.getValue(Conversation.class) );
                                        conversationDataResourceMutableLiveData.setValue(DataResource.success(dataSnapshot.getValue(Conversation.class)));

                                    }else
                                    {
                                        DatabaseReference myRef = firebaseDatabase.getReference("conversations");
                                        final String conversationId = myRef.push().getKey();
                                        final Person curentUserAsPerson = new Person(
                                                user.getId(),
                                                user.getName(),
                                                user.getPhoto(),
                                                user.getBio(),
                                                user.getStatus(),
                                                user.getEmail(),
                                                user.getCity(),
                                                user.getMobile()
                                        );

                                        List<Person> people = new ArrayList<>();
                                        people.add(curentUserAsPerson);
                                        people.add(person);

                                        Long currentDate = Long.parseLong(new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date()));

                                        HashMap<String, Message> messages = new HashMap<>();

                                        messages.put("-1",  new Message(
                                                "-1",
                                                "Welcome",
                                                curentUserAsPerson.getName() + " asking you for conversation?",
                                                "",
                                                "",
                                                "",
                                                currentDate,
                                                curentUserAsPerson,
                                                "Sended"
                                        ));

                                        HashMap<String, Object> unreadMessagesStringObjectHashMap = new HashMap<>();
                                        unreadMessagesStringObjectHashMap.put(curentUserAsPerson.getId(),0);
                                        unreadMessagesStringObjectHashMap.put(person.getId(),1);

                                        final Conversation conversation = new Conversation(
                                                conversationId,
                                                "",
                                                (Message) messages.get("-1"),
                                                currentDate,
                                                people,
                                                messages,
                                                curentUserAsPerson.getId() + "asking" + person.getId() + "",
                                                unreadMessagesStringObjectHashMap
                                                ,messages.get("-1").getId()
                                        );


                                        myRef.child(conversationId).setValue(conversation.toMap()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                DatabaseReference myRef = firebaseDatabase.getReference("users").child(curentUserAsPerson.getId()).child("conversations").child(person.getId());
                                                myRef.setValue(conversation.toMapWithoutMessages()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        DatabaseReference myRef2 = firebaseDatabase.getReference("users").child(person.getId()).child("conversations").child(curentUserAsPerson.getId());
                                                        myRef2.setValue(conversation.toMapWithoutMessages()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                conversationDataResourceMutableLiveData.setValue(DataResource.success(conversation));
                                                                Log.e(TAG, "onDataChange: conversation created" + conversationId );
                                                            }
                                                        });

                                                    }
                                                });
                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: " + e.getMessage() );
                        conversationDataResourceMutableLiveData.setValue(DataResource.error("Can not start Conversation " , (Conversation) null));
                    }

                    @Override
                    public void onComplete() {
                        Log.e(TAG, "onComplete: " );
                    }
                });

        return conversationDataResourceMutableLiveData;
    }
}
