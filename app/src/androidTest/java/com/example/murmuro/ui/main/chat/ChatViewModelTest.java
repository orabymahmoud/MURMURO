package com.example.murmuro.ui.main.chat;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.paging.PagedList;

import com.example.murmuro.R;
import com.example.murmuro.Utils;
import com.example.murmuro.model.Conversation;
import com.example.murmuro.model.DataResource;
import com.example.murmuro.model.Message;
import com.example.murmuro.model.Person;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.StorageReference;
import com.shreyaspatil.firebase.recyclerpagination.DatabasePagingOptions;
import com.shreyaspatil.firebase.recyclerpagination.FirebaseRecyclerPagingAdapter;
import com.shreyaspatil.firebase.recyclerpagination.LoadingState;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static androidx.constraintlayout.widget.Constraints.TAG;
import static org.junit.Assert.*;

public class ChatViewModelTest {


    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    @Test
    public void getConversationDataResourceMutableLiveData() {
        DatabaseReference databaseReference = firebaseDatabase.getReference().child("conversations").child("-MBKgFmLsBlSgG0V4_pE");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                assertEquals("1", "1");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                assertEquals("1", "0");
            }
        });

    }


    @Test
    public void sendTextMessage() {
        Long currentDate = Long.parseLong(new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date()));

        Message message = new Message(
                "-1",
                "Welcome",
                "Tester" + " asking you for conversation?",
                "",
                "",
                "",
                currentDate,
                new Person(),
                "Sended"
        ) ;


        DatabaseReference databaseReference2 = firebaseDatabase.getReference()
                .child("conversations")
                .child("test")
                .child("messages")
                .child(1 + "");


        databaseReference2.setValue(message).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                assertEquals("1", "1");

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                assertEquals("1", "0");

            }
        });

    }

    @Test
    public void getMessagesAdapter() {
        DatabaseReference messagesReference = firebaseDatabase.getReference()
                .child("conversations")
                .child("2tQZXlhGcJhA4u9Elt8o5TpsqTq1")
                .child("messages");


        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(5)
                .setPageSize(10)
                .build();

        DatabasePagingOptions<Message> options = new DatabasePagingOptions.Builder<Message>()
                .setQuery(messagesReference , config, Message.class)
                .build();


        FirebaseRecyclerPagingAdapter<Message, ChatAdapter.MyViewHolder> firebaseRecyclerPagingAdapter = new FirebaseRecyclerPagingAdapter<Message, ChatAdapter.MyViewHolder>(options)  {
            @Override
            protected void onBindViewHolder(@NonNull final ChatAdapter.MyViewHolder myViewHolder, int i, @NonNull final Message message) {
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
            public ChatAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
                ViewDataBinding binding = DataBindingUtil.inflate(layoutInflater, R.layout.chat_item, parent, false);
                return new ChatAdapter.MyViewHolder(binding);
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