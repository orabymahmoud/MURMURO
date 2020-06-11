package com.example.murmuro.di;

import com.example.murmuro.di.auth.AuthFragmentBuildersModule;
import com.example.murmuro.di.auth.AuthModule;
import com.example.murmuro.di.auth.AuthScope;
import com.example.murmuro.di.auth.AuthViewModelsModule;
import com.example.murmuro.di.main.MainFragmentBuildersModule;
import com.example.murmuro.di.main.MainModule;
import com.example.murmuro.di.main.MainScope;
import com.example.murmuro.di.main.MainViewModelsModule;
import com.example.murmuro.ui.Splash;
import com.example.murmuro.ui.auth.AuthActivity;
import com.example.murmuro.ui.main.MainActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ActivityBuildersModule {


    @AuthScope
    @ContributesAndroidInjector(modules = {AuthViewModelsModule.class, AuthModule.class, AuthFragmentBuildersModule.class})
    abstract AuthActivity contributeAuthActivityActivity();

    @MainScope
    @ContributesAndroidInjector(modules = {MainViewModelsModule.class, MainModule.class, MainFragmentBuildersModule.class})
    abstract MainActivity contributeMainActivityActivity();

}
