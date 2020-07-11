package com.example.murmuro.ui.auth.vervication.confirmation;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.test.InstrumentationRegistry;

import com.example.murmuro.model.AuthResource;
import com.example.murmuro.model.Conversation;
import com.example.murmuro.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.Test;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.assertEquals;

public class Confirmation_Test {

    Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    private String verificationid;
    private FirebaseAuth mAuth;


    @Test
    public void sensendVerificationCode()
    {
        sendVerificationCode("+2001010752704");
    }


    private void sendVerificationCode(String number){

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallBack
        );
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationid = s;
        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code != null){

                assertEquals("1", "1");

            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            assertEquals("1", "0");

        }
    };


    @Test
    public void  registerUser()
    {
        String phone = "0123456789";
        String city = "Egypt";
        String name = "Tester";
        String username ="test";
        String email = "test@util.com";
        String password = "123456789000";
        String bio = "test";
        String photo ="defaultPhoto.png";
        String status = "testing";

        final User user = new User("", name, username, password, photo, email, city, phone, bio, status, new HashMap<String, Conversation>());

        mAuth = FirebaseAuth.getInstance();

        mAuth.createUserWithEmailAndPassword(user.getUsername(),user.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful())
                {
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("users");

                    user.setId(mAuth.getCurrentUser().getUid());
                    myRef.child(mAuth.getCurrentUser().getUid()).setValue(user.toMap()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                assertEquals("1", "1");
                            }else
                            {
                                assertEquals("1", "0");
                            }
                        }
                    });


                }else
                {
                    assertEquals("1", "0");

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                assertEquals("1", "0");

            }
        });

    }

}