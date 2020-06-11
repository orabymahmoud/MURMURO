package com.example.murmuro.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.example.murmuro.R;
import com.example.murmuro.databinding.ActivitySplashBinding;
import com.example.murmuro.model.Conversation;
import com.example.murmuro.model.Message;
import com.example.murmuro.model.User;
import com.example.murmuro.ui.auth.AuthActivity;
import com.example.murmuro.ui.main.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class Splash extends AppCompatActivity {

    private ActivitySplashBinding binding;
    private Handler handler;
    private FirebaseAuth mAuth;
    private static final String TAG = "Splash";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash);

        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/Pacifico-Regular.ttf");
        binding.textView.setTypeface(typeface);



        handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                mAuth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = mAuth.getCurrentUser();
                Log.e(TAG, "run: " + currentUser );

                if(currentUser != null)
                {
                    Intent intent=new Intent(Splash.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }else
                {
                    Intent intent=new Intent(Splash.this, AuthActivity.class);
                    startActivity(intent);
                    finish();
                }


            }
        },3000);

    }




}
