package com.example.murmuro.ui.main.people;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.navigation.Navigation;
import androidx.paging.PagedList;

import com.example.murmuro.R;
import com.example.murmuro.model.DataResource;
import com.example.murmuro.model.Person;
import com.example.murmuro.ui.main.MainActivity;
import com.github.abdularis.civ.CircleImageView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.shreyaspatil.firebase.recyclerpagination.DatabasePagingOptions;
import com.shreyaspatil.firebase.recyclerpagination.FirebaseRecyclerPagingAdapter;
import com.shreyaspatil.firebase.recyclerpagination.LoadingState;

import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicMarkableReference;

import static android.view.View.GONE;
import static org.junit.Assert.*;

public class PeopleViewModelTest {

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    @Test
    public void getPersonsAdapter() {
        DatabaseReference userDatabaseReference = firebaseDatabase.getReference("users");

        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(5)
                .setPageSize(10)
                .build();

        DatabasePagingOptions<Person> options = new DatabasePagingOptions.Builder<Person>()
                .setQuery(userDatabaseReference , config, Person.class)
                .build();

        FirebaseRecyclerPagingAdapter<Person, PeopleAdapter.MyViewHolder> firebaseRecyclerPagingAdapter
                = new FirebaseRecyclerPagingAdapter<Person, PeopleAdapter.MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull PeopleAdapter.MyViewHolder myViewHolder, int i, @NonNull final Person person) {
                assertEquals("1", "1");
            }


            @Override
            protected void onLoadingStateChanged(@NonNull LoadingState state) {
                switch (state) {
                    case LOADING_INITIAL:
                    case LOADING_MORE:
                        // Do your loading animation
                        break;

                    case LOADED:
                        // Stop Animation

                    case FINISHED:
                        //Reached end of Data set

                        break;

                    case ERROR:
                        assertEquals("1", "0");
                        retry();
                        break;
                }
            }

            @NonNull
            @Override
            public PeopleAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
                ViewDataBinding binding = DataBindingUtil.inflate(layoutInflater, R.layout.people_item, parent, false);
                return new PeopleAdapter.MyViewHolder(binding);
            }

            @Override
            protected void onError(@NonNull DatabaseError databaseError) {
                super.onError(databaseError);
                databaseError.toException().printStackTrace();
                assertEquals("1", "0");

            }
        };

    }

    @Test
    public void isInternetAvailable() {
        try {
            final String command = "ping -c 1 google.com";

            assertEquals(Runtime.getRuntime().exec(command).waitFor(), 0);

        } catch (Exception e) {
            assertEquals(1, 0);

        }
    }

}