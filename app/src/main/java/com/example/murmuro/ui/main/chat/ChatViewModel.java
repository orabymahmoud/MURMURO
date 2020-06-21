package com.example.murmuro.ui.main.chat;

import android.app.Activity;
import android.content.Context;
import android.media.AudioRouting;
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
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.PagedList;

import com.bumptech.glide.RequestManager;
import com.example.murmuro.R;
import com.example.murmuro.model.Conversation;
import com.example.murmuro.model.DataResource;
import com.example.murmuro.model.Message;
import com.example.murmuro.model.Person;
import com.example.murmuro.model.User;
import com.example.murmuro.storage.room.MurmuroRepositoryImp;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.otaliastudios.cameraview.controls.Audio;
import com.shreyaspatil.firebase.recyclerpagination.DatabasePagingOptions;
import com.shreyaspatil.firebase.recyclerpagination.FirebaseRecyclerPagingAdapter;
import com.shreyaspatil.firebase.recyclerpagination.LoadingState;


import java.io.File;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static androidx.constraintlayout.widget.Constraints.TAG;

public class ChatViewModel extends ViewModel {

    private MurmuroRepositoryImp murmuroRepositoryImp;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    private FirebaseStorage firebaseStorage;
    RequestManager requestManager;
    private Context context;
    private Activity activity;
    private MutableLiveData<DataResource<User>> currentUserDataResourceMutableLiveData = new MutableLiveData<>();
    private MutableLiveData<DataResource<Conversation>> conversationDataResourceMutableLiveData = new MutableLiveData<>();
    private MutableLiveData<DataResource<FirebaseRecyclerPagingAdapter<Message, ChatAdapter.MyViewHolder>>>  messagesAdapterDataResourceMutableLiveData = new MutableLiveData<>();
    private int unreadMessages;
    private String c_date = "-1";
    private String c_time = "-1";
    private int wordsIndex = 0;
    private Handler wordsHandler ;
    private Runnable wordsRunnable ;
    private TextToSpeech t1;

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }




    @Inject
    public ChatViewModel(MurmuroRepositoryImp murmuroRepositoryImp,
                         FirebaseDatabase firebaseDatabase,
                         FirebaseAuth firebaseAuth,
                         FirebaseStorage firebaseStorage,
                         RequestManager requestManager) {
        this.murmuroRepositoryImp = murmuroRepositoryImp;
        this.firebaseDatabase = firebaseDatabase;
        this.firebaseAuth = firebaseAuth;
        this.firebaseStorage = firebaseStorage;
        this.requestManager = requestManager;
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


    public void sendStorageMessage(final Message message, final Person friendUser, final String conversatId, final long messagesSize)
    {

        StorageReference ref = null;

        if(message.getMessageType().equals("File"))
        {
             ref = firebaseStorage.getReference().child("files/"+ conversatId + "/" + message.getDateTime()+message.getText());

        }else if(message.getMessageType().equals("Audio"))
        {
             ref = firebaseStorage.getReference().child("audios/"+ conversatId + "/" + message.getDateTime()+message.getText());

        }else if(message.getMessageType().equals("Video"))
        {
             ref = firebaseStorage.getReference().child("videos/"+ conversatId + "/" + message.getDateTime()+message.getText());

        }else if(message.getMessageType().equals("Gif"))
        {
             ref = firebaseStorage.getReference().child("gifs/"+ conversatId + "/" + message.getDateTime()+message.getText());

        }else if(message.getMessageType().equals("Photo"))
        {
             ref = firebaseStorage.getReference().child("images/"+ conversatId + "/" + message.getDateTime()+message.getText());
        }


        ref.putFile(Uri.parse(message.getPhoto()))
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.e(TAG, "onSuccess: Uploaded" );
                        Toast.makeText(context, "Start Uploading", Toast.LENGTH_SHORT).show();

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
                        Toast.makeText(context, "Uploaded Success", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: Failed" );
                        Toast.makeText(context, "Uploaded : Failed", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                .getTotalByteCount());
                        Log.e(TAG, "onProgress: Uploaded" + (int)progress+"%");

                    }
                });



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


    public MutableLiveData<DataResource<FirebaseRecyclerPagingAdapter<Message, ChatAdapter.MyViewHolder>>> getMessagesAdapter(final String conversationId, final Person friendUser, final LifecycleOwner lifecycleOwner)
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
            protected void onBindViewHolder(@NonNull final ChatAdapter.MyViewHolder myViewHolder, int i, @NonNull final Message message) {
                myViewHolder.bind(message);

                myViewHolder.itemView.findViewById(R.id.message_layout).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(myViewHolder.itemView.findViewById(R.id.message_options).getVisibility() != GONE) {
                            myViewHolder.itemView.findViewById(R.id.message_options).setVisibility(GONE);

                        }
                    }
                });

                myViewHolder.itemView.findViewById(R.id.message_layout).setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if(myViewHolder.itemView.findViewById(R.id.message_options).getVisibility() == GONE)
                        {
                            myViewHolder.itemView.findViewById(R.id.message_options).setVisibility(View.VISIBLE);

                            if(message.getMessageType().equals("Text") || message.getMessageType().equals("Welcome") )
                            {
                                myViewHolder.itemView.findViewById(R.id.download_file_option).setVisibility(GONE);
                            }else if (message.getMessageType().equals("Photo"))
                            {
                                myViewHolder.itemView.findViewById(R.id.translate_to_sign_option).setVisibility(GONE);
                                myViewHolder.itemView.findViewById(R.id.convert_to_voice_message_option).setVisibility(GONE);
                            }else if (message.getMessageType().equals("File"))
                            {
                                myViewHolder.itemView.findViewById(R.id.translate_to_sign_option).setVisibility(GONE);
                                myViewHolder.itemView.findViewById(R.id.convert_to_voice_message_option).setVisibility(GONE);
                            }else if (message.getMessageType().equals("Gif"))
                            {
                                myViewHolder.itemView.findViewById(R.id.translate_to_sign_option).setVisibility(GONE);
                                myViewHolder.itemView.findViewById(R.id.convert_to_voice_message_option).setVisibility(GONE);
                            }else if (message.getMessageType().equals("Audio"))
                            {
                                myViewHolder.itemView.findViewById(R.id.translate_to_sign_option).setVisibility(GONE);
                                myViewHolder.itemView.findViewById(R.id.convert_to_voice_message_option).setVisibility(GONE);
                            }else if (message.getMessageType().equals("Video"))
                            {
                                myViewHolder.itemView.findViewById(R.id.translate_to_sign_option).setVisibility(GONE);
                                myViewHolder.itemView.findViewById(R.id.convert_to_voice_message_option).setVisibility(GONE);
                            }



                            if(!message.getSentBy().getId().equals(firebaseAuth.getCurrentUser().getUid()))
                            {
                                myViewHolder.itemView.findViewById(R.id.delete_message_option).setVisibility(GONE);
                            }else
                            {
                                myViewHolder.itemView.findViewById(R.id.delete_message_option).setVisibility(VISIBLE);
                            }

                        }else {
                            myViewHolder.itemView.findViewById(R.id.message_options).setVisibility(GONE);
                        }
                        return true;
                    }
                });
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
                        converstaion_date.setText(message.getDateTime().toString().substring(6,8) + " - " + message.getDateTime().toString().substring(4,6));
                        c_date = message.getDateTime().toString().substring(6,8);
                    }else
                    {
                        converstaion_date.setVisibility(View.GONE);
                    }

                    if(!c_time.equals(message.getDateTime().toString().substring(8,10)))
                    {

                        c_time = message.getDateTime().toString().substring(8,10);

                        converstaion_time.setText(message.getDateTime().toString().substring(8,10) + " : " +message.getDateTime().toString().substring(10,12));
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
                    final MaterialTextView message_textMaterialTextView =  myViewHolder.itemView.findViewById(R.id.s_message_text);
                    final ImageView message_photoImageView =  myViewHolder.itemView.findViewById(R.id.s_message_photo);
                    final VideoView message_videoVideoView =  myViewHolder.itemView.findViewById(R.id.s_message_video);
                    LinearLayout message_audioLinearLayout =  myViewHolder.itemView.findViewById(R.id.s_message_audio);
                    final ImageView message_audio_playImageView =  myViewHolder.itemView.findViewById(R.id.s_message_audio_play);
                    final SeekBar message_audio_seekBarSeekBar =  myViewHolder.itemView.findViewById(R.id.s_message_audio_seekBar);
                    final MaterialTextView message_audio_timeMaterialTextView =  myViewHolder.itemView.findViewById(R.id.s_message_audio_time);
                    MaterialTextView  message_timeMaterialTextView =  myViewHolder.itemView.findViewById(R.id.s_message_time);
                    LinearLayout file_layout = myViewHolder.itemView.findViewById(R.id.s_file_layout);
                    final ImageView  dowenloadFile = myViewHolder.itemView.findViewById(R.id.s_downloadFile);
                    TextView fileName = myViewHolder.itemView.findViewById(R.id.s_fileName);

                    if(message.getMessageType().equals("Text") || message.getMessageType().equals("Welcome") )
                    {
                        message_textMaterialTextView.setText(message.getText());
                    }else if (message.getMessageType().equals("Photo"))
                    {
                        message_textMaterialTextView.setVisibility(GONE);
                        message_photoImageView.setVisibility(VISIBLE);
                        firebaseStorage.getReference().child("images/"+ conversationId + "/" + message.getDateTime()  + message.getText())
                                .getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        requestManager.load(uri.toString()).into(message_photoImageView);
                                    }
                                });
                    }else if (message.getMessageType().equals("File"))
                    {
                        message_textMaterialTextView.setVisibility(GONE);
                        file_layout.setVisibility(VISIBLE);
                        fileName.setText(message.getText());

                    }else if (message.getMessageType().equals("Video"))
                    {
                        message_videoVideoView.setVisibility(VISIBLE);
                        message_textMaterialTextView.setVisibility(GONE);

                        firebaseStorage.getReference().child("videos/"+ conversationId + "/" + message.getDateTime()  + message.getText())
                                .getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {

                                        message_videoVideoView.setVideoURI(uri);
                                        message_textMaterialTextView.setVisibility(VISIBLE);
                                        message_textMaterialTextView.setText("Loading.");
                                        message_videoVideoView.requestFocus();
                                        message_videoVideoView.start();

                                        message_videoVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                            @Override
                                            public void onPrepared(MediaPlayer mp) {
                                                mp.setLooping(true);
                                            }
                                        });

                                        message_videoVideoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                                            @Override
                                            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                                                MediaController mediaController = null;
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                                    mediaController = new MediaController(context);
                                                }
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                                    message_videoVideoView.setMediaController(mediaController);
                                                }
                                                mediaController.setAnchorView(message_videoVideoView);

                                                if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                                                    message_textMaterialTextView.setVisibility(GONE);
                                                    return true;
                                                }
                                                else if(what == MediaPlayer.MEDIA_INFO_BUFFERING_START){
                                                    message_textMaterialTextView.setVisibility(VISIBLE);
                                                    message_textMaterialTextView.setText("Loading..");
                                                    return true;
                                                }
                                                return false;
                                            }
                                        });

                                    }
                                });

                    }else if (message.getMessageType().equals("Audio"))
                    {
                        message_textMaterialTextView.setVisibility(GONE);
                        message_audioLinearLayout.setVisibility(VISIBLE);

                        firebaseStorage.getReference().child("audios/"+ conversationId + "/" + message.getDateTime()  + message.getText())
                                .getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        final MediaPlayer mp = new MediaPlayer();
                                        try {
                                            mp.setDataSource(context, uri);
                                            mp.prepare();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        message_audio_seekBarSeekBar.setMax(mp.getDuration());

                                        final Handler mSeekbarUpdateHandler = new Handler();
                                        final Runnable mUpdateSeekbar = new Runnable() {
                                            @Override
                                            public void run() {
                                                message_audio_seekBarSeekBar.setProgress(mp.getCurrentPosition());
                                                mSeekbarUpdateHandler.postDelayed(this, 50);
                                            }
                                        };

                                        message_audio_timeMaterialTextView.setText(getTimeString(mp.getDuration()));
                                        message_audio_playImageView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if(mp.isPlaying()){
                                                   mp.pause();
                                                    message_audio_seekBarSeekBar.removeCallbacks(mUpdateSeekbar);
                                                    message_audio_playImageView.setImageResource(R.drawable.ic_play);
                                                }else{
                                                    message_audio_playImageView.setImageResource(R.drawable.ic_pause);
                                                    mp.start();
                                                    mSeekbarUpdateHandler.postDelayed(mUpdateSeekbar, 0);
                                                    Log.e(TAG, "onClick: " + mp.getCurrentPosition() );
                                                    Log.e(TAG, "onClick: " + mp.getDuration() );
                                                }
                                            }
                                        });


                                        message_audio_seekBarSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                            @Override
                                            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                                                if (b)
                                                    mp.seekTo(i);
                                                Log.e(TAG, "onProgressChanged: " + i );
                                            }

                                            @Override
                                            public void onStartTrackingTouch(SeekBar seekBar) {

                                            }

                                            @Override
                                            public void onStopTrackingTouch(SeekBar seekBar) {

                                            }
                                        });

                                    }
                                });


                    }else if (message.getMessageType().equals("Gif"))
                    {
                        message_textMaterialTextView.setVisibility(GONE);
                        message_photoImageView.setVisibility(VISIBLE);
                        firebaseStorage.getReference().child("gifs/"+ conversationId + "/" + message.getDateTime()  + message.getText())
                                .getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        requestManager.asGif().load(uri.toString()).into(message_photoImageView);
                                    }
                                });
                    }

                    if(message.getId().equals("-1"))
                    {
                        converstaion_time.setText(message.getDateTime().toString().substring(8,10) + " : " +message.getDateTime().toString().substring(10,12));
                        converstaion_date.setText(message.getDateTime().toString().substring(6,8) + " - " + message.getDateTime().toString().substring(4,6));
                        c_date = message.getDateTime().toString().substring(8,10);
                        c_time = message.getDateTime().toString().substring(4,6);
                    }

                    // Message options
                    myViewHolder.itemView.findViewById(R.id.translate_to_sign_option).setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v) {
                            if(message.getMessageType().equals("Text") || message.getMessageType().equals("Welcome") )
                            {
                                String text = message.getText();
                                message_photoImageView.setVisibility(VISIBLE);

                                final List<String> words = new ArrayList<>();

                                String word = "";

                                for(int i=0; i < text.length();i++)
                                {
                                    if(text.charAt(i) == ' ')
                                    {
                                        Log.e(TAG, "onClick: add " + word );
                                        words.add(word);
                                        word = "";
                                    }else
                                    {
                                        word += text.charAt(i);
                                    }
                                }

                                if(!word.equals(""))
                                {
                                    words.add(word);
                                    Log.e(TAG, "onClick: add " + word );
                                }


                                wordsHandler = new Handler();
                                wordsRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        if(wordsIndex < words.size())
                                        {
                                            firebaseStorage.getReference().child("Signs/" + words.get(wordsIndex) + ".gif").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    requestManager.asGif().load(uri.toString()).into(message_photoImageView);
                                                    if(wordsIndex < words.size())
                                                    {
                                                        Log.e(TAG, "onSuccess: loaded a " + words.get(wordsIndex) );
                                                    }
                                                }
                                            });
                                            wordsIndex++;
                                        }else
                                        {
                                            wordsIndex = 0;
                                            wordsHandler = null;
                                            wordsRunnable = null;
                                            message_photoImageView.setVisibility(GONE);
                                            myViewHolder.itemView.findViewById(R.id.message_options).setVisibility(GONE);
                                        }

                                        if(wordsHandler != null || wordsRunnable != null)
                                        {
                                            wordsHandler.postDelayed(this, 1000);
                                        }
                                    }
                                };
                                if(wordsHandler != null || wordsRunnable != null)
                                {
                                    wordsHandler.postDelayed(wordsRunnable, 1000);
                                }

                            }
                        }
                    });





                    message_timeMaterialTextView.setText(
                            message.getDateTime().toString().substring(8,10) + " : " +message.getDateTime().toString().substring(10,12)
                    );

                    t1=new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
                            if(status != TextToSpeech.ERROR) {
                                t1.setLanguage(Locale.UK);
                            }
                        }
                    });

                    myViewHolder.itemView.findViewById(R.id.convert_to_voice_message_option).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            t1.speak(message.getText(), TextToSpeech.QUEUE_FLUSH, null);
                        }
                    });

                    myViewHolder.itemView.findViewById(R.id.download_file_option).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            StorageReference ref = null;

                            String filePath = "";

                            if(message.getMessageType().equals("File"))
                            {
                                ref = firebaseStorage.getReference().child("files/"+ conversationId + "/" + message.getDateTime()+message.getText());
                                filePath = "Murmuro/Files";
                            }else if(message.getMessageType().equals("Audio"))
                            {
                                ref = firebaseStorage.getReference().child("audios/"+ conversationId + "/" + message.getDateTime()+ message.getText());
                                filePath = "Murmuro/Audios";
                            }else if(message.getMessageType().equals("Video"))
                            {
                                ref = firebaseStorage.getReference().child("videos/"+ conversationId + "/" + message.getDateTime()+ message.getText());
                                filePath = "Murmuro/Videos";
                            }else if(message.getMessageType().equals("Gif"))
                            {
                                ref = firebaseStorage.getReference().child("gifs/"+ conversationId + "/" + message.getDateTime()+ message.getText());
                                filePath = "Murmuro/Gifs";
                            }else if(message.getMessageType().equals("Photo"))
                            {
                                ref = firebaseStorage.getReference().child("images/"+ conversationId + "/" + message.getDateTime()+ message.getText());
                                filePath = "Murmuro/Photos";
                            }


                            File fileNameOnDevice = null;


                            final File folder = new File(Environment.getExternalStorageDirectory() +
                                    File.separator + filePath);
                            boolean success = true;
                            if (!folder.exists()) {
                                success = folder.mkdirs();
                            }
                            if (success) {
                                // Do something on success
                                fileNameOnDevice = new File(folder + "/"+ message.getText());
                            } else {
                                // Do something else on failure
                            }

                            Toast.makeText(context, "Start Downloading", Toast.LENGTH_SHORT).show();

                            ref.getFile(fileNameOnDevice).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    Log.e(TAG, "onSuccess: downloaded in " + folder.getName() );
                                    Toast.makeText(context, "downloaded in " + folder.getName() + " folder" , Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });


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

                }else if(message.getSentBy().getId().equals(friendUser.getId()))
                {
                    recieved_messageLinearLayout.setVisibility(View.VISIBLE);
                    sended_messageLinearLayout.setVisibility(View.GONE);

                    final MaterialTextView message_textMaterialTextView =  myViewHolder.itemView.findViewById(R.id.message_text);
                    final ImageView  message_photoImageView =  myViewHolder.itemView.findViewById(R.id.message_photo);
                    final VideoView message_videoVideoView =  myViewHolder.itemView.findViewById(R.id.message_video);
                    LinearLayout message_audioLinearLayout =  myViewHolder.itemView.findViewById(R.id.message_audio);
                    final ImageView message_audio_playImageView =  myViewHolder.itemView.findViewById(R.id.message_audio_play);
                    final SeekBar message_audio_seekBarSeekBar =  myViewHolder.itemView.findViewById(R.id.message_audio_seekBar);
                    final MaterialTextView message_audio_timeMaterialTextView =  myViewHolder.itemView.findViewById(R.id.message_audio_time);
                    MaterialTextView message_timeMaterialTextView =  myViewHolder.itemView.findViewById(R.id.message_time);
                    LinearLayout file_layout = myViewHolder.itemView.findViewById(R.id.file_layout);
                    ImageView dowenloadFile = myViewHolder.itemView.findViewById(R.id.downloadFile);
                    TextView fileName = myViewHolder.itemView.findViewById(R.id.fileName);

                    if(message.getMessageType().equals("Text") || message.getMessageType().equals("Welcome") )
                    {
                        message_textMaterialTextView.setText(message.getText());
                    }else if (message.getMessageType().equals("Photo"))
                    {
                        message_textMaterialTextView.setVisibility(GONE);
                        message_photoImageView.setVisibility(VISIBLE);
                        firebaseStorage.getReference().child("images/"+ conversationId + "/" + message.getDateTime()  + message.getText())
                                .getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        requestManager.load(uri.toString()).into(message_photoImageView);
                                    }
                                });
                    }else if (message.getMessageType().equals("File"))
                    {
                        message_textMaterialTextView.setVisibility(GONE);
                        file_layout.setVisibility(VISIBLE);
                        fileName.setText(message.getText());

                    }else if (message.getMessageType().equals("Video"))
                    {
                        message_videoVideoView.setVisibility(VISIBLE);
                        message_textMaterialTextView.setVisibility(GONE);

                        firebaseStorage.getReference().child("videos/"+ conversationId + "/" + message.getDateTime()  + message.getText())
                                .getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {

                                        message_videoVideoView.setVideoURI(uri);
                                        message_textMaterialTextView.setVisibility(VISIBLE);
                                        message_textMaterialTextView.setText("Loading.");
                                        message_videoVideoView.requestFocus();
                                        message_videoVideoView.start();

                                        message_videoVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                            @Override
                                            public void onPrepared(MediaPlayer mp) {
                                                mp.setLooping(true);
                                            }
                                        });

                                        message_videoVideoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                                            @Override
                                            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                                                MediaController mediaController = null;
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                                    mediaController = new MediaController(context);
                                                }
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                                    message_videoVideoView.setMediaController(mediaController);
                                                }
                                                mediaController.setAnchorView(message_videoVideoView);

                                                if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                                                    message_textMaterialTextView.setVisibility(GONE);
                                                    return true;
                                                }
                                                else if(what == MediaPlayer.MEDIA_INFO_BUFFERING_START){
                                                    message_textMaterialTextView.setVisibility(VISIBLE);
                                                    message_textMaterialTextView.setText("Loading..");
                                                    return true;
                                                }
                                                return false;
                                            }
                                        });

                                    }
                                });

                    }else if (message.getMessageType().equals("Audio"))
                    {
                        message_textMaterialTextView.setVisibility(GONE);
                        message_audioLinearLayout.setVisibility(VISIBLE);

                        firebaseStorage.getReference().child("audios/"+ conversationId + "/" + message.getDateTime()  + message.getText())
                                .getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        final MediaPlayer mp = new MediaPlayer();
                                        try {
                                            mp.setDataSource(context, uri);
                                            mp.prepare();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        message_audio_seekBarSeekBar.setMax(mp.getDuration());

                                        final Handler mSeekbarUpdateHandler = new Handler();
                                        final Runnable mUpdateSeekbar = new Runnable() {
                                            @Override
                                            public void run() {
                                                message_audio_seekBarSeekBar.setProgress(mp.getCurrentPosition());
                                                mSeekbarUpdateHandler.postDelayed(this, 50);
                                            }
                                        };

                                        message_audio_timeMaterialTextView.setText(getTimeString(mp.getDuration()));
                                        message_audio_playImageView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if(mp.isPlaying()){
                                                    mp.pause();
                                                    message_audio_seekBarSeekBar.removeCallbacks(mUpdateSeekbar);
                                                    message_audio_playImageView.setImageResource(R.drawable.ic_play);
                                                }else{
                                                    message_audio_playImageView.setImageResource(R.drawable.ic_pause);
                                                    mp.start();
                                                    mSeekbarUpdateHandler.postDelayed(mUpdateSeekbar, 0);
                                                    Log.e(TAG, "onClick: " + mp.getCurrentPosition() );
                                                    Log.e(TAG, "onClick: " + mp.getDuration() );
                                                }
                                            }
                                        });


                                        message_audio_seekBarSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                            @Override
                                            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                                                if (b)
                                                    mp.seekTo(i);
                                                Log.e(TAG, "onProgressChanged: " + i );
                                            }

                                            @Override
                                            public void onStartTrackingTouch(SeekBar seekBar) {

                                            }

                                            @Override
                                            public void onStopTrackingTouch(SeekBar seekBar) {

                                            }
                                        });

                                    }
                                });


                    }else if (message.getMessageType().equals("Gif"))
                    {
                        message_textMaterialTextView.setVisibility(GONE);
                        message_photoImageView.setVisibility(VISIBLE);
                        firebaseStorage.getReference().child("gifs/"+ conversationId + "/" + message.getDateTime()  + message.getText())
                                .getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        requestManager.asGif().load(uri.toString()).into(message_photoImageView);
                                    }
                                });
                    }


                    if(message.getId().equals("-1"))
                    {
                        converstaion_time.setText(message.getDateTime().toString().substring(8,10) + " : " +message.getDateTime().toString().substring(10,12));
                        converstaion_date.setText(message.getDateTime().toString().substring(6,8) + " - " + message.getDateTime().toString().substring(4,6));
                        c_date = message.getDateTime().toString().substring(8,10);
                        c_time = message.getDateTime().toString().substring(4,6);
                    }

                    // Message options

                    myViewHolder.itemView.findViewById(R.id.translate_to_sign_option).setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v) {
                            if(message.getMessageType().equals("Text") || message.getMessageType().equals("Welcome") )
                            {
                                String text = message.getText();
                                message_photoImageView.setVisibility(VISIBLE);

                                final List<String> words = new ArrayList<>();

                                String word = "";

                                for(int i=0; i < text.length();i++)
                                {
                                    if(text.charAt(i) == ' ')
                                    {
                                        Log.e(TAG, "onClick: add " + word );
                                        words.add(word);
                                        word = "";
                                    }else
                                    {
                                        word += text.charAt(i);
                                    }
                                }

                                if(!word.equals(""))
                                {
                                    words.add(word);
                                    Log.e(TAG, "onClick: add " + word );
                                }


                                wordsHandler = new Handler();
                                wordsRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        if(wordsIndex < words.size())
                                        {
                                            firebaseStorage.getReference().child("Signs/" + words.get(wordsIndex) + ".gif").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    requestManager.asGif().load(uri.toString()).into(message_photoImageView);
                                                    if(wordsIndex < words.size())
                                                    {
                                                        Log.e(TAG, "onSuccess: loaded a " + words.get(wordsIndex) );
                                                    }
                                                }
                                            });
                                            wordsIndex++;
                                        }else
                                        {
                                            wordsIndex = 0;
                                            wordsHandler = null;
                                            wordsRunnable = null;
                                            message_photoImageView.setVisibility(GONE);
                                            myViewHolder.itemView.findViewById(R.id.message_options).setVisibility(GONE);
                                        }

                                        if(wordsHandler != null || wordsRunnable != null)
                                        {
                                            wordsHandler.postDelayed(this, 1000);
                                        }
                                    }
                                };
                                if(wordsHandler != null || wordsRunnable != null)
                                {
                                    wordsHandler.postDelayed(wordsRunnable, 1000);
                                }

                            }
                        }
                    });

                    message_timeMaterialTextView.setText(
                            message.getDateTime().toString().substring(8,10) + " : " +message.getDateTime().toString().substring(10,12)
                    );


                    t1=new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
                            if(status != TextToSpeech.ERROR) {
                                t1.setLanguage(Locale.UK);
                            }
                        }
                    });

                    t1=new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
                            if(status != TextToSpeech.ERROR) {
                                t1.setLanguage(Locale.UK);
                            }
                        }
                    });

                    myViewHolder.itemView.findViewById(R.id.convert_to_voice_message_option).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            t1.speak(message.getText(), TextToSpeech.QUEUE_FLUSH, null);
                        }
                    });

                    message_timeMaterialTextView.setText(
                            message.getDateTime().toString().substring(8,10) + " : " +message.getDateTime().toString().substring(10,12)
                    );


                    myViewHolder.itemView.findViewById(R.id.download_file_option).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            StorageReference ref = null;

                            String filePath = "";

                            if(message.getMessageType().equals("File"))
                            {
                                ref = firebaseStorage.getReference().child("files/"+ conversationId + "/" + message.getDateTime()+message.getText());
                                filePath = "Murmuro/Files";
                            }else if(message.getMessageType().equals("Audio"))
                            {
                                ref = firebaseStorage.getReference().child("audios/"+ conversationId + "/" + message.getDateTime()+ message.getText());
                                filePath = "Murmuro/Audios";
                            }else if(message.getMessageType().equals("Video"))
                            {
                                ref = firebaseStorage.getReference().child("videos/"+ conversationId + "/" + message.getDateTime()+ message.getText());
                                filePath = "Murmuro/Videos";
                            }else if(message.getMessageType().equals("Gif"))
                            {
                                ref = firebaseStorage.getReference().child("gifs/"+ conversationId + "/" + message.getDateTime()+ message.getText());
                                filePath = "Murmuro/Gifs";
                            }else if(message.getMessageType().equals("Photo"))
                            {
                                ref = firebaseStorage.getReference().child("images/"+ conversationId + "/" + message.getDateTime()+ message.getText());
                                filePath = "Murmuro/Photos";
                            }


                            File fileNameOnDevice = null;


                            final File folder = new File(Environment.getExternalStorageDirectory() +
                                    File.separator + filePath);
                            boolean success = true;
                            if (!folder.exists()) {
                                success = folder.mkdirs();
                            }
                            if (success) {
                                // Do something on success
                                fileNameOnDevice = new File(folder + "/"+ message.getText());
                            } else {
                                // Do something else on failure
                            }

                            Toast.makeText(context, "Start Downloading", Toast.LENGTH_SHORT).show();

                            ref.getFile(fileNameOnDevice).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    Log.e(TAG, "onSuccess: downloaded in " + folder.getName() );
                                    Toast.makeText(context, "downloaded in " + folder.getName() + " folder" , Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });

                }



                myViewHolder.itemView.findViewById(R.id.delete_message_option).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DatabaseReference databaseReference2 = firebaseDatabase.getReference()
                                .child("conversations")
                                .child(conversationId)
                                .child("messages")
                                .child(message.getId());

                        databaseReference2.setValue(null);
                    }
                });

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

    private String getTimeString(long millis) {
        StringBuffer buf = new StringBuffer();

        int hours = (int) (millis / (1000 * 60 * 60));
        int minutes = (int) ((millis % (1000 * 60 * 60)) / (1000 * 60));
        int seconds = (int) (((millis % (1000 * 60 * 60)) % (1000 * 60)) / 1000);

        buf
                .append(String.format("%02d", hours))
                .append(":")
                .append(String.format("%02d", minutes))
                .append(":")
                .append(String.format("%02d", seconds));

        return buf.toString();
    }

}
