package com.example.murmuro.di.auth;

import androidx.lifecycle.ViewModel;

import com.example.murmuro.di.ViewModelKey;
import com.example.murmuro.ui.auth.createNew.CreateNewViewModel;
import com.example.murmuro.ui.auth.logIn.LogInViewModel;
import com.example.murmuro.ui.auth.vervication.confirmation.ConfirmationViewModel;
import com.example.murmuro.ui.main.livetranslation.LiveTranslationViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class AuthViewModelsModule {
    @Binds
    @IntoMap
    @ViewModelKey(LogInViewModel.class)
    public abstract ViewModel bindLogInViewModel(LogInViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(CreateNewViewModel.class)
    public abstract ViewModel bindCreateNewViewModel(CreateNewViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(ConfirmationViewModel.class)
    public abstract ViewModel bindConfirmationViewModel(ConfirmationViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(LiveTranslationViewModel.class)
    public abstract ViewModel bindLiveTranslationViewModel(LiveTranslationViewModel viewModel);
}
