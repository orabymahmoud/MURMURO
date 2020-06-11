package com.example.murmuro.ui.auth.vervication.confirmation;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.murmuro.model.User;
import com.example.murmuro.storage.room.MurmuroRepositoryImp;
import com.example.murmuro.model.AuthResource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import javax.inject.Inject;

public class ConfirmationViewModel extends ViewModel {
    private FirebaseAuth mAuth;
    private MutableLiveData<AuthResource<User>> userLiveData = new MutableLiveData<>();
    private String TAG = "ConfirmationViewModel";
    private MurmuroRepositoryImp murmuroRepositoryImp;

    public Context authActivity;

    public Context getAuthActivity() {
        return authActivity;
    }

    public void setAuthActivity(Context authActivity) {
        this.authActivity = authActivity;
    }

    @Inject
    public ConfirmationViewModel(MurmuroRepositoryImp murmuroRepositoryImp) {
        this.murmuroRepositoryImp = murmuroRepositoryImp;
    }

    public LiveData<AuthResource<User>> registerUser(final User user) {
        userLiveData.setValue(AuthResource.loading((User) (null)));

        mAuth = FirebaseAuth.getInstance();

        mAuth.createUserWithEmailAndPassword(user.getUsername(),user.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful())
                {
                    Toast.makeText(authActivity, "User created", Toast.LENGTH_SHORT).show();
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("users");

                    user.setId(mAuth.getCurrentUser().getUid());

                    myRef.child(mAuth.getCurrentUser().getUid()).setValue(user.toMap()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                userLiveData.setValue(AuthResource.authenticated(user));
                                murmuroRepositoryImp.insertUSer(user);
                            }else
                            {
                                userLiveData.setValue(AuthResource.error("Error while creating new user" , (User) null));
                            }
                        }
                    });


                }else
                {
                    Toast.makeText(authActivity, "Authentication failed", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(authActivity, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return userLiveData;
    }

}
