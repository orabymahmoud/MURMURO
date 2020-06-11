package com.example.murmuro.di;

import android.app.Application;
import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.example.murmuro.R;
import com.example.murmuro.model.User;
import com.example.murmuro.storage.room.MurmuroDatabaseClient;
import com.example.murmuro.storage.room.MurmuroRepositoryImp;
import com.example.murmuro.ui.auth.AuthActivity;
import com.example.murmuro.ui.main.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.net.InetAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.functions.Consumer;


@Module
public class AppModule {


    @Singleton
    @Provides
    Context provideContext(Application application) {
        return application;
    }


    @Singleton
    @Provides
    public static MurmuroRepositoryImp providesMurmuroRepositoryImp(Context context, Executor executor, FirebaseDatabase firebaseDatabase)
    {
        return new MurmuroRepositoryImp(new MurmuroDatabaseClient(context).getMurmuroDatabase().murmuroDao() , executor);
    }

    @Singleton
    @Provides
    public static Executor providesExecutor()
    {
        return Executors.newCachedThreadPool();
    }

    @Singleton
    @Provides
    public static FirebaseDatabase providesFirebaseDatabase()
    {return FirebaseDatabase.getInstance();}

    @Singleton
    @Provides
    public static FirebaseAuth providesFirebaseAuth()
    {return FirebaseAuth.getInstance();}

    @Singleton
    @Provides
    public static FirebaseStorage providesFirebaseStorage()
    {return FirebaseStorage.getInstance();}

    @Singleton
    @Provides
    static RequestOptions provideRequestOptions(){
        return RequestOptions
                .placeholderOf(R.drawable.ic_busy)
                .error(R.drawable.ic_close);
    }

    @Singleton
    @Provides
    static RequestManager provideGlideInstance(Application application, RequestOptions requestOptions){
        return Glide.with(application)
                .setDefaultRequestOptions(requestOptions);
    }

}
