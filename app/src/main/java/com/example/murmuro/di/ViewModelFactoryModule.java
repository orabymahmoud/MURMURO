package com.example.murmuro.di;

import androidx.lifecycle.ViewModelProvider;

import com.example.murmuro.viewModel.ViewModelProviderFactory;

import dagger.Binds;
import dagger.Module;

@Module
public abstract class ViewModelFactoryModule {

    @Binds
    public abstract ViewModelProvider.Factory bindViewModelFactory(ViewModelProviderFactory viewModelFactory);

}
