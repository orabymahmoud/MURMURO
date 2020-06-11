package com.example.murmuro.di.main;

import androidx.lifecycle.ViewModel;

import com.example.murmuro.di.ViewModelKey;
import com.example.murmuro.ui.main.calls.CallsViewModel;
import com.example.murmuro.ui.main.chat.ChatViewModel;
import com.example.murmuro.ui.main.conversations.ConversationsViewModel;
import com.example.murmuro.ui.main.editprofile.EditProfileViewModel;
import com.example.murmuro.ui.main.groups.GroupsViewModel;
import com.example.murmuro.ui.main.livetranslation.LiveTranslation;
import com.example.murmuro.ui.main.livetranslation.LiveTranslationViewModel;
import com.example.murmuro.ui.main.people.PeopleViewModel;
import com.example.murmuro.ui.main.personprofile.PersonProfileViewModel;
import com.example.murmuro.ui.main.profile.ProfileViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class MainViewModelsModule {

    @Binds
    @IntoMap
    @ViewModelKey(ConversationsViewModel.class)
    public abstract ViewModel bindConversationsViewModel(ConversationsViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(ChatViewModel.class)
    public abstract ViewModel bindChatViewModel(ChatViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(PeopleViewModel.class)
    public abstract ViewModel bindPeopleViewModel(PeopleViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(GroupsViewModel.class)
    public abstract ViewModel bindGroupsViewModel(GroupsViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(CallsViewModel.class)
    public abstract ViewModel bindCallsViewModel(CallsViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(LiveTranslationViewModel.class)
    public abstract ViewModel bindLiveTranslationViewModel(LiveTranslationViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(ProfileViewModel.class)
    public abstract ViewModel bindProfileViewModel(ProfileViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(EditProfileViewModel.class)
    public abstract ViewModel bindEditProfileViewModel(EditProfileViewModel viewModel);

    @Binds
    @IntoMap
    @ViewModelKey(PersonProfileViewModel.class)
    public abstract ViewModel bindPersonProfileViewModel(PersonProfileViewModel viewModel);
}
