package com.example.murmuro.ui.main.profile;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bumptech.glide.RequestManager;
import com.example.murmuro.model.DataResource;
import com.example.murmuro.model.User;
import com.example.murmuro.storage.room.MurmuroRepositoryImp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class ProfileViewModel extends ViewModel {

    private MurmuroRepositoryImp murmuroRepositoryImp;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    private FirebaseStorage firebaseStorage;
    private RequestManager requestManager;
    private Activity activity;
    private MutableLiveData<DataResource<User>> currentUserDataResourceMutableLiveData = new MutableLiveData<>();


    @Inject
    public ProfileViewModel(MurmuroRepositoryImp murmuroRepositoryImp, FirebaseDatabase firebaseDatabase, FirebaseAuth firebaseAuth,FirebaseStorage firebaseStorage,RequestManager requestManager) {
        this.murmuroRepositoryImp = murmuroRepositoryImp;
        this.firebaseDatabase = firebaseDatabase;
        this.firebaseAuth = firebaseAuth;
        this.firebaseStorage = firebaseStorage;
        this.requestManager = requestManager;
    }

    public MutableLiveData<DataResource<User>> getCurrentUserDataResourceMutableLiveData()
    {

        currentUserDataResourceMutableLiveData.setValue(DataResource.loading((User) null));

        murmuroRepositoryImp.getUserById(firebaseAuth.getCurrentUser().getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(new Function<Throwable, User>() {
                    @Override
                    public User apply(Throwable throwable) throws Exception {
                        User user = new User();
                        user.setId("-1");
                        return user;
                    }
                }).map(new Function<User, Object>() {
            @Override
            public Object apply(User user) throws Exception {
                if(user.getId().equals("-1"))
                {
                    currentUserDataResourceMutableLiveData.setValue(DataResource.error("can not load user" , (User) null));
                    return null;
                }

                currentUserDataResourceMutableLiveData.setValue(DataResource.success(user));
                return user;
            }
        }).subscribe();


        return currentUserDataResourceMutableLiveData;
    }

    public void updateCurrentUser()
    {
        DatabaseReference myRef = firebaseDatabase.
                getReference("users").
                child(firebaseAuth.getCurrentUser().getUid());

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                murmuroRepositoryImp.insertUSer(user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

}
