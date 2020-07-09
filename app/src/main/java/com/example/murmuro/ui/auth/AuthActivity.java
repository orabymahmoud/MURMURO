package com.example.murmuro.ui.auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.Navigation;

import android.os.Bundle;
import android.util.Log;

import com.example.murmuro.BaseActivity;
import com.example.murmuro.R;

import dagger.android.support.DaggerAppCompatActivity;

public class AuthActivity extends BaseActivity {

    private static final String TAG = "AuthActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBundle("nav_state", Navigation.findNavController(this, R.id.nav_host_fragment).saveState());

    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Navigation.findNavController(this, R.id.nav_host_fragment).restoreState(savedInstanceState.getBundle("nav_state"));
        Navigation.findNavController(this, R.id.nav_host_fragment).setGraph(R.navigation.auth_nav);
        Log.e(TAG, "onRestoreInstanceState: " + Navigation.findNavController(this, R.id.nav_host_fragment).getCurrentDestination().getLabel());
    }

    }
