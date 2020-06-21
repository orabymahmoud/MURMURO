package com.example.murmuro.di.main;

import com.example.murmuro.ui.main.calls.Calls;
import com.example.murmuro.ui.main.chat.Chat;
import com.example.murmuro.ui.main.conversations.Conversations;
import com.example.murmuro.ui.main.groups.Groups;
import com.example.murmuro.ui.main.livetranslation.LiveTranslation;
import com.example.murmuro.ui.main.people.People;
import com.example.murmuro.ui.main.personprofile.PersonProfile;
import com.example.murmuro.ui.main.profile.Profile;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class MainFragmentBuildersModule {

    @ContributesAndroidInjector
    abstract Conversations ContributesConversationFragment();

    @ContributesAndroidInjector
    abstract Chat ContributesChatFragment();

    @ContributesAndroidInjector
    abstract People ContributesPeopleFragment();

    @ContributesAndroidInjector
    abstract Groups ContributesGroupsFragment();

    @ContributesAndroidInjector
    abstract Calls ContributesCallsFragment();

    @ContributesAndroidInjector
    abstract LiveTranslation ContributesLiveTranslationFragment();

    @ContributesAndroidInjector
    abstract Profile ContributesProfileFragment();


    @ContributesAndroidInjector
    abstract PersonProfile ContributesPersonProfile();
}
