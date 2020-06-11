package com.example.murmuro.ui.auth.logIn;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.murmuro.model.User;
import com.example.murmuro.storage.room.MurmuroRepositoryImp;
import com.example.murmuro.model.AuthResource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import javax.inject.Inject;

public class LogInViewModel extends ViewModel {

    private FirebaseAuth mAuth;
    private MutableLiveData<AuthResource<User>> mutableLiveData = new MutableLiveData<>();

    public MurmuroRepositoryImp murmuroRepositoryImp;


    public Context authActivity;

    private static final String TAG = "LogInViewModel";

    public Context getAuthActivity() {
        return authActivity;
    }

    public void setAuthActivity(Context authActivity) {
        this.authActivity = authActivity;
    }

    @Inject
    public LogInViewModel(MurmuroRepositoryImp murmuroRepositoryImp) {
        this.murmuroRepositoryImp = murmuroRepositoryImp;
    }


    public LiveData<AuthResource<User>> LogIn(String username, String password) {
        mutableLiveData.setValue(AuthResource.loading((User) null));
        mAuth = FirebaseAuth.getInstance();

        mAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener((Activity) authActivity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            final FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef = database.getReference("users").child(firebaseUser.getUid());

                            myRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    User user = dataSnapshot.getValue(User.class);

                                    if(user != null)
                                    {
                                        murmuroRepositoryImp.insertUSer(user);
                                    }
                                    mutableLiveData.setValue(AuthResource.authenticated(user));
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                    mutableLiveData.setValue(AuthResource.error(databaseError.getMessage(),(User) null));
                                }
                            });


                        } else {
                            mutableLiveData.setValue(AuthResource.error(task.getException().getMessage(),(User) null));
                        }
                    }
                });

        return mutableLiveData;
    }

}
