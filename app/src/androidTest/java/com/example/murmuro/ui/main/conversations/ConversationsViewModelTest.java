package com.example.murmuro.ui.main.conversations;

import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.navigation.Navigation;
import androidx.paging.PagedList;

import com.example.murmuro.R;
import com.example.murmuro.model.Conversation;
import com.example.murmuro.model.DataResource;
import com.example.murmuro.model.Person;
import com.example.murmuro.model.User;
import com.example.murmuro.ui.main.MainActivity;
import com.github.abdularis.civ.CircleImageView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shreyaspatil.firebase.recyclerpagination.DatabasePagingOptions;
import com.shreyaspatil.firebase.recyclerpagination.FirebaseRecyclerPagingAdapter;
import com.shreyaspatil.firebase.recyclerpagination.LoadingState;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static android.view.View.GONE;
import static org.junit.Assert.*;

public class ConversationsViewModelTest {


    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    @Test
    public void getConversationAdapter() {

        DatabaseReference conversationDatabaseReference = firebaseDatabase.
                getReference("users").
                child("2tQZXlhGcJhA4u9Elt8o5TpsqTq1").
                child("conversations");

        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(5)
                .setPageSize(10)
                .build();



        DatabasePagingOptions<Conversation> options = new DatabasePagingOptions.Builder<Conversation>()
                .setQuery(conversationDatabaseReference , config, Conversation.class)
                .build();


        FirebaseRecyclerPagingAdapter<Conversation, ConversationAdapter.MyViewHolder> firebaseRecyclerPagingAdapter
                = new FirebaseRecyclerPagingAdapter<Conversation, ConversationAdapter.MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ConversationAdapter.MyViewHolder myViewHolder, int i, @NonNull final Conversation conversation) {
                assertEquals("1", "1");
                Log.d("TAG", "onBindViewHolder: " + conversation.toString());
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
                        break;

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
            public ConversationAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
                ViewDataBinding binding = DataBindingUtil.inflate(layoutInflater, R.layout.conversation_item, parent, false);
                return new ConversationAdapter.MyViewHolder(binding);
            }

            @Override
            protected void onError(@NonNull DatabaseError databaseError) {
                super.onError(databaseError);
                databaseError.toException().printStackTrace();
                assertEquals("1", "0");

            }
        };
    }

}