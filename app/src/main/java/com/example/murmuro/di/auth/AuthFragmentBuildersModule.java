package com.example.murmuro.di.auth;

import com.example.murmuro.ui.auth.createNew.CreateNew;
import com.example.murmuro.ui.auth.logIn.LogIn;
import com.example.murmuro.ui.auth.vervication.confirmation.Confirmation;
import com.example.murmuro.ui.auth.vervication.MobileNumber;
import com.example.murmuro.ui.main.livetranslation.LiveTranslation;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class AuthFragmentBuildersModule {

    @ContributesAndroidInjector
    abstract LogIn ContributesLogInFragment();

    @ContributesAndroidInjector
    abstract CreateNew ContributesCreateNewFragment();

    @ContributesAndroidInjector
    abstract MobileNumber ContributesMobileNumberFragment();

    @ContributesAndroidInjector
    abstract Confirmation ContributesConfirmationFragment();

    @ContributesAndroidInjector
    abstract LiveTranslation ContributesLiveTranslation();
}
