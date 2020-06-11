package com.example.murmuro.ui.main.chat;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.PagedList;

import com.example.murmuro.R;
import com.example.murmuro.model.Conversation;
import com.example.murmuro.model.DataResource;
import com.example.murmuro.model.Message;
import com.example.murmuro.model.Person;
import com.example.murmuro.model.User;
import com.example.murmuro.storage.room.MurmuroRepositoryImp;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shreyaspatil.firebase.recyclerpagination.DatabasePagingOptions;
import com.shreyaspatil.firebase.recyclerpagination.FirebaseRecyclerPagingAdapter;
import com.shreyaspatil.firebase.recyclerpagination.LoadingState;


import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class ChatViewModel extends ViewModel {

    private MurmuroRepositoryImp murmuroRepositoryImp;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    private Activity activity;
    private MutableLiveData<DataResource<User>> currentUserDataResourceMutableLiveData = new MutableLiveData<>();
    private MutableLiveData<DataResource<Conversation>> conversationDataResourceMutableLiveData = new MutableLiveData<>();
    private MutableLiveData<DataResource<FirebaseRecyclerPagingAdapter<Message, ChatAdapter.MyViewHolder>>>  messagesAdapterDataResourceMutableLiveData = new MutableLiveData<>();
    private int unreadMessages;
    private String c_date = "-1";
    private String c_time = "-1";
    @Inject

    public ChatViewModel(MurmuroRepositoryImp murmuroRepositoryImp, FirebaseDatabase firebaseDatabase, FirebaseAuth firebaseAuth) {
        this.murmuroRepositoryImp = murmuroRepositoryImp;
        this.firebaseDatabase = firebaseDatabase;
        this.firebaseAuth = firebaseAuth;
    }

    public MutableLiveData<DataResource<User>> getCurrentUserDataResourceMutableLiveData()
    {

        currentUserDataResourceMutableLiveData.setValue(DataResource.loading((User) null));

        murmuroRepositoryImp.getUserById(firebaseAuth.getCurrentUser().getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(new Function<Throwable, User>() {
                    @Override
                    public User apply(Throwable throwable) throws Exception {
                        User user = new User();
                        user.setId("-1");
                        return user;
                    }
                }).map(new Function<User, Object>() {
            @Override
            public Object apply(User user) throws Exception {
                if(user.getId().equals("-1"))
                {
                    currentUserDataResourceMutableLiveData.setValue(DataResource.error("can not load user" , (User) null));
                    return null;
                }

                currentUserDataResourceMutableLiveData.setValue(DataResource.success(user));
                return user;
            }
        }).subscribe();


        return currentUserDataResourceMutableLiveData;
    }

    public MutableLiveData<DataResource<Conversation>> getConversationDataResourceMutableLiveData(String conversationId) {

        conversationDataResourceMutableLiveData.setValue(DataResource.loading((Conversation) null));

        if(isInternetAvailable())
        {
            DatabaseReference databaseReference = firebaseDatabase.getReference().child("conversations").child(conversationId);

            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    Log.e(TAG, "onDataChange: " + dataSnapshot.getValue().toString() );

                    Conversation conversation = dataSnapshot.getValue(Conversation.class);
                    murmuroRepositoryImp.updateConversation(conversation);
                    conversationDataResourceMutableLiveData.setValue(DataResource.success(conversation));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    conversationDataResourceMutableLiveData.setValue(DataResource.error(databaseError.getMessage() , (Conversation) null));
                }
            });

        }else if(!isInternetAvailable())
        {
            murmuroRepositoryImp.getConversationById(conversationId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .onErrorReturn(new Function<Throwable, Conversation>() {
                        @Override
                        public Conversation apply(Throwable throwable) throws Exception {
                            Conversation conversation = new Conversation();
                            conversation.setId("-1");
                            return null;
                        }
                    })
                    .map(new Function<Conversation, Object>() {
                        @Override
                        public Object apply(Conversation conversation) throws Exception {
                            if(conversation.getId().equals("-1"))
                            {
                                conversationDataResourceMutableLiveData.setValue(DataResource.error("Can not load conversation", (Conversation) null));
                                return null;
                            }

                            conversationDataResourceMutableLiveData.setValue(DataResource.success(conversation));
                            return conversation;
                        }
                    }).subscribe();
        }

        return conversationDataResourceMutableLiveData;
    }


    public void sendTextMessage(final Message message, final Person friendUser, final String conversatId, long messagesSize)
    {

        DatabaseReference databaseReference1 = firebaseDatabase.getReference()
                .child("conversations")
                .child(conversatId)
                .child("lastMessageId");

        databaseReference1.setValue(messagesSize + "");

        DatabaseReference databaseReference2 = firebaseDatabase.getReference()
                .child("conversations")
                .child(conversatId)
                .child("messages")
                .child(((messagesSize) + ""));


        databaseReference2.setValue(message);

        final DatabaseReference databaseReference3 = firebaseDatabase.getReference()
                .child("conversations")
                .child(conversatId)
                .child("undreadMessages")
                .child(friendUser.getId());


        databaseReference3.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                unreadMessages = dataSnapshot.getValue(Integer.class);
                unreadMessages++;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: " + "can not load unread messages" );
            }
        });

        databaseReference3.setValue(unreadMessages);

        DatabaseReference databaseReference4 = firebaseDatabase.getReference()
                .child("users")
                .child(friendUser.getId())
                .child("conversations")
                .child(firebaseAuth.getCurrentUser().getUid())
                .child("undreadMessages")
                .child(friendUser.getId());

        databaseReference4.setValue(unreadMessages);

        DatabaseReference databaseReference5 = firebaseDatabase.getReference()
                .child("users")
                .child(friendUser.getId())
                .child("conversations")
                .child(firebaseAuth.getCurrentUser().getUid())
                .child("displayMessage");

        databaseReference5.setValue(message);

        DatabaseReference databaseReference6 = firebaseDatabase.getReference()
                .child("users")
                .child(firebaseAuth.getCurrentUser().getUid())
                .child("conversations")
                .child(friendUser.getId())
                .child("displayMessage");

        databaseReference6.setValue(message);

    }


    public MutableLiveData<DataResource<FirebaseRecyclerPagingAdapter<Message, ChatAdapter.MyViewHolder>>> getMessagesAdapter(final String conversationId, final Person friendUser, LifecycleOwner lifecycleOwner)
    {

       DatabaseReference messagesReference = firebaseDatabase.getReference()
                .child("conversations")
                .child(conversationId)
                .child("messages");


        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(5)
                .setPageSize(10)
                .build();

        DatabasePagingOptions<Message> options = new DatabasePagingOptions.Builder<Message>()
                .setLifecycleOwner(lifecycleOwner)
                .setQuery(messagesReference , config, Message.class)
                .build();


        FirebaseRecyclerPagingAdapter<Message, ChatAdapter.MyViewHolder>  firebaseRecyclerPagingAdapter = new FirebaseRecyclerPagingAdapter<Message, ChatAdapter.MyViewHolder>(options)  {
            @Override
            protected void onBindViewHolder(@NonNull ChatAdapter.MyViewHolder myViewHolder, int i, @NonNull Message message) {
                myViewHolder.bind(message);

                if(!message.getStatus().equals("Seen") && !message.getSentBy().getId().equals(firebaseAuth.getCurrentUser().getUid()))
                {
                    DatabaseReference messageSatatusDatabaseReference = firebaseDatabase
                            .getReference()
                            .child("conversations")
                            .child(conversationId)
                            .child("messages")
                            .child(message.getId())
                            .child("status");
                    messageSatatusDatabaseReference.setValue("Seen");
                }


                DatabaseReference usersUnreadMessagesDatabaseReference = firebaseDatabase
                        .getReference()
                        .child("users")
                        .child(firebaseAuth.getCurrentUser().getUid())
                        .child("conversations")
                        .child(friendUser.getId())
                        .child("undreadMessages")
                        .child(firebaseAuth.getCurrentUser().getUid());

                usersUnreadMessagesDatabaseReference.setValue(0);

                LinearLayout sended_messageLinearLayout = myViewHolder.itemView.findViewById(R.id.sended_message);
                LinearLayout recieved_messageLinearLayout = myViewHolder.itemView.findViewById(R.id.recieved_message);
                TextView converstaion_time = myViewHolder.itemView.findViewById(R.id.converstaion_time);
                TextView converstaion_date = myViewHolder.itemView.findViewById(R.id.converstaion_date);

                if(!message.getId().equals("-1"))
                {
                    if(!c_date.equals(message.getDateTime().toString().substring(6,8)))
                    {
                        converstaion_date.setText(message.getDateTime().toString().substring(4,6) + "-" +message.getDateTime().toString().substring(6,8));
                        c_date = message.getDateTime().toString().substring(6,8);
                    }else
                    {
                        converstaion_date.setVisibility(View.GONE);
                    }

                    if(!c_time.equals(message.getDateTime().toString().substring(8,10)))
                    {

                        c_time = message.getDateTime().toString().substring(8,10);

                        converstaion_time.setText(message.getDateTime().toString().substring(8,10) + ":" +message.getDateTime().toString().substring(10,12));
                    }else
                    {
                        converstaion_time.setVisibility(View.GONE);
                    }
                }


                if(message.getSentBy().getId().equals(firebaseAuth.getCurrentUser().getUid()))
                {
                   recieved_messageLinearLayout.setVisibility(View.GONE);
                   sended_messageLinearLayout.setVisibility(View.VISIBLE);
                    ImageView message_statusImageView = myViewHolder.itemView.findViewById(R.id.message_status);
                    MaterialTextView message_textMaterialTextView =  myViewHolder.itemView.findViewById(R.id.s_message_text);
                    ImageView message_photoImageView =  myViewHolder.itemView.findViewById(R.id.s_message_photo);
                    VideoView message_videoVideoView =  myViewHolder.itemView.findViewById(R.id.s_message_video);
                    LinearLayout message_audioLinearLayout =  myViewHolder.itemView.findViewById(R.id.s_message_audio);
                    ImageView message_audio_playImageView =  myViewHolder.itemView.findViewById(R.id.s_message_audio_play);
                    SeekBar message_audio_seekBarSeekBar =  myViewHolder.itemView.findViewById(R.id.s_message_audio_seekBar);
                    MaterialTextView message_audio_timeMaterialTextView =  myViewHolder.itemView.findViewById(R.id.s_message_audio_time);
                    MaterialTextView message_timeMaterialTextView =  myViewHolder.itemView.findViewById(R.id.s_message_time);


                    if(message.getStatus().equals("Seen"))
                    {
                        message_statusImageView.setImageResource(R.drawable.ic_online);
                    }else if(message.getStatus().equals("Arrived"))
                    {
                        message_statusImageView.setImageResource(R.drawable.ic_away);

                    }else if(message.getStatus().equals("Sended"))
                    {
                        message_statusImageView.setImageResource(R.drawable.ic_busy);

                    }

                    if(message.getMessageType().equals("Text") || message.getMessageType().equals("Welcome") )
                    {
                        message_textMaterialTextView.setText(message.getText());
                    }

                    message_timeMaterialTextView.setText(
                            message.getDateTime().toString().substring(8,10) + ":" +message.getDateTime().toString().substring(10,12)
                    );

                    if(message.getId().equals("-1"))
                    {
                        converstaion_time.setText(message.getDateTime().toString().substring(8,10) + ":" +message.getDateTime().toString().substring(10,12));
                        converstaion_date.setText(message.getDateTime().toString().substring(4,6) + "-" +message.getDateTime().toString().substring(6,8));
                        c_date = message.getDateTime().toString().substring(8,10);
                        c_time = message.getDateTime().toString().substring(4,6);
                    }
                }else if(message.getSentBy().getId().equals(friendUser.getId()))
                {
                    recieved_messageLinearLayout.setVisibility(View.VISIBLE);
                    sended_messageLinearLayout.setVisibility(View.GONE);
                    MaterialTextView message_textMaterialTextView =  myViewHolder.itemView.findViewById(R.id.message_text);
                    ImageView message_photoImageView =  myViewHolder.itemView.findViewById(R.id.message_photo);
                    VideoView message_videoVideoView =  myViewHolder.itemView.findViewById(R.id.message_video);
                    LinearLayout message_audioLinearLayout =  myViewHolder.itemView.findViewById(R.id.message_audio);
                    ImageView message_audio_playImageView =  myViewHolder.itemView.findViewById(R.id.message_audio_play);
                    SeekBar message_audio_seekBarSeekBar =  myViewHolder.itemView.findViewById(R.id.message_audio_seekBar);
                    MaterialTextView message_audio_timeMaterialTextView =  myViewHolder.itemView.findViewById(R.id.message_audio_time);
                    MaterialTextView message_timeMaterialTextView =  myViewHolder.itemView.findViewById(R.id.message_time);



                    if(message.getMessageType().equals("Text") || message.getMessageType().equals("Welcome") )
                    {
                        message_textMaterialTextView.setText(message.getText());
                    }



                    message_timeMaterialTextView.setText(
                            message.getDateTime().toString().substring(8,10) + ":" +message.getDateTime().toString().substring(10,12)
                    );
                    if(message.getId().equals("-1"))
                    {
                        converstaion_time.setText(message.getDateTime().toString().substring(8,10) + ":" +message.getDateTime().toString().substring(10,12));
                        converstaion_date.setText(message.getDateTime().toString().substring(4,6) + "-" +message.getDateTime().toString().substring(6,8));
                        c_date = message.getDateTime().toString().substring(8,10);
                        c_time = message.getDateTime().toString().substring(4,6);
                    }
                }



            }

            @Override
            protected void onLoadingStateChanged(@NonNull LoadingState state) {
                switch (state) {
                    case LOADING_INITIAL:
                    case LOADING_MORE:
                        // Do your loading animation
                       messagesAdapterDataResourceMutableLiveData.setValue(DataResource.loading((FirebaseRecyclerPagingAdapter<Message, ChatAdapter.MyViewHolder>)null));
                        break;

                    case LOADED:
                        // Stop Animation
                        messagesAdapterDataResourceMutableLiveData.setValue(DataResource.error("LOADED" , (FirebaseRecyclerPagingAdapter<Message, ChatAdapter.MyViewHolder>)null));
                        break;

                    case FINISHED:
                        //Reached end of Data set
                        messagesAdapterDataResourceMutableLiveData.setValue(DataResource.error("FINISHED" , (FirebaseRecyclerPagingAdapter<Message, ChatAdapter.MyViewHolder>)null));

                        break;

                    case ERROR:
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
            }
        };

        messagesAdapterDataResourceMutableLiveData.setValue(DataResource.success(firebaseRecyclerPagingAdapter));

        return  messagesAdapterDataResourceMutableLiveData;
    }



    private boolean isInternetAvailable() {
        try {
            final String command = "ping -c 1 google.com";
            return Runtime.getRuntime().exec(command).waitFor() == 0;
        } catch (Exception e) {
            Log.e(TAG, "ProvidesisInternetAvailable: " + e.getMessage());
            return false;
        }
    }



}
