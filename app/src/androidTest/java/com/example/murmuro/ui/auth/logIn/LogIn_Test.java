package com.example.murmuro.ui.auth.logIn;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.Test;
import org.junit.runner.RunWith;

import static android.content.ContentValues.TAG;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class LogIn_Test {

     Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

    @Test
    public void forgetPasswordDialog() {

        assertEquals("com.example.murmuro", appContext.getPackageName());

        FirebaseAuth.getInstance().sendPasswordResetEmail("oraby.elgen@gmail.com")
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        assertEquals("1", "0");
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email sent.");
                            assertEquals("1", "1");

                        }
                    }
                });
    }

    @Test
    public void observeLogIn() {

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        mAuth.signInWithEmailAndPassword("g@h.com", "hdYyy555$")
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    assertEquals("1", "1");
                }else{
                    assertEquals("1", "0");
                }
            }
        });
    }
}