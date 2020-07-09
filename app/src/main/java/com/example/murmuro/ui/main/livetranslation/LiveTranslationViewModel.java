package com.example.murmuro.ui.main.livetranslation;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class LiveTranslationViewModel extends ViewModel {
    // TODO: Implement the ViewModel


    @Inject
    public LiveTranslationViewModel() {
    }


    public boolean isInternetAvailable()
    {
        try {
            final String command = "ping -c 1 google.com";
            return Runtime.getRuntime().exec(command).waitFor() == 0;
        } catch (Exception e) {
            Log.e(TAG, "ProvidesisInternetAvailable: " + e.getMessage());
            return false;
        }
    }

}
