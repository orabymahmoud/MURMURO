package com.example.murmuro.ui.main.chat;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.bumptech.glide.RequestManager;
import com.example.murmuro.R;
import com.example.murmuro.databinding.ChatFragmentBinding;
import com.example.murmuro.machineLearning.Classifier;
import com.example.murmuro.machineLearning.TensorFlowImageClassifier;
import com.example.murmuro.model.Conversation;
import com.example.murmuro.model.DataResource;
import com.example.murmuro.model.Message;
import com.example.murmuro.model.Person;
import com.example.murmuro.model.User;
import com.example.murmuro.ui.main.MainActivity;
import com.example.murmuro.viewModel.ViewModelProviderFactory;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.otaliastudios.cameraview.BitmapCallback;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.controls.Audio;
import com.shreyaspatil.firebase.recyclerpagination.DatabasePagingOptions;
import com.shreyaspatil.firebase.recyclerpagination.FirebaseRecyclerPagingAdapter;
import com.shreyaspatil.firebase.recyclerpagination.LoadingState;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class Chat extends DaggerFragment {

    private ChatViewModel mViewModel;

    ChatFragmentBinding binding;

    @Inject
    ViewModelProviderFactory providerFactory;

    @Inject
    FirebaseStorage firebaseStorage;

    @Inject
    FirebaseDatabase firebaseDatabase;

    @Inject
    RequestManager requestManager;

    FirebaseRecyclerPagingAdapter firebaseRecyclerPagingAdapter;

    Person frienUser = null;
    Person curentUserAsPerson = null;
    long lastMesssageIndex;

    private String conversationId = "";

    private static final String MODEL_PATH = "model.tflite";
    private static final boolean QUANT = true;
    private static final String LABEL_PATH = "Labels.txt";
    private static final int INPUT_SIZE = 40;
    private Classifier classifier;
    private Executor executor = Executors.newSingleThreadExecutor();

    public static Chat newInstance() {
        return new Chat();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.chat_fragment, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this, providerFactory).get(ChatViewModel.class);
        if(savedInstanceState != null)
        {
            Navigation.findNavController(getActivity(), R.id.host_fragment).restoreState(savedInstanceState.getBundle("nav_state"));
            Log.e(TAG, "onRestoreInstanceState: " +  savedInstanceState.getBundle("nav_state").describeContents());
        }

        conversationId = ChatArgs.fromBundle(getArguments()).getConversationId();

        mViewModel.getCurrentUserDataResourceMutableLiveData().observe(getActivity(), new Observer<DataResource<User>>() {
            @Override
            public void onChanged(final DataResource<User> userDataResource) {
                if(userDataResource != null)
                {
                    switch (userDataResource.status)
                    {
                        case ERROR:{
                            binding.progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), userDataResource.message ,Toast.LENGTH_LONG).show();
                            break;
                        }

                        case LOADING:{
                            binding.progressBar.setVisibility(View.VISIBLE);
                            break;
                        }

                        case SUCCESS:{
                            binding.progressBar.setVisibility(View.GONE);

                            User user = userDataResource.data;
                            curentUserAsPerson = new Person(
                                    user.getId(),
                                    user.getName(),
                                    user.getPhoto(),
                                    user.getBio(),
                                    user.getStatus(),
                                    user.getEmail(),
                                    user.getCity(),
                                    user.getMobile()
                            );

                            mViewModel.getConversationDataResourceMutableLiveData(conversationId)
                                    .observe(getActivity(), new Observer<DataResource<Conversation>>() {
                                        @Override
                                        public void onChanged(DataResource<Conversation> conversationDataResource) {
                                            if(conversationDataResource != null)
                                            {
                                                switch (conversationDataResource.status)
                                                {
                                                    case ERROR:{
                                                        binding.progressBar.setVisibility(View.GONE);
                                                        Toast.makeText(getContext(), conversationDataResource.message ,Toast.LENGTH_LONG).show();
                                                        break;
                                                    }

                                                    case LOADING:{
                                                        binding.progressBar.setVisibility(View.VISIBLE);
                                                        break;
                                                    }

                                                    case SUCCESS:{
                                                        binding.progressBar.setVisibility(View.GONE);

                                                        if(userDataResource.data.getId().equals(conversationDataResource.data.getMembers().get(0).getId()))
                                                        {
                                                            frienUser = conversationDataResource.data.getMembers().get(1);

                                                        }else {
                                                            frienUser = conversationDataResource.data.getMembers().get(0);

                                                        }


                                                        lastMesssageIndex = Long.parseLong(conversationDataResource.data.getLastMessageId());

                                                        DatabaseReference databaseReference = firebaseDatabase.getReference().child("users").child(frienUser.getId());

                                                        databaseReference.addValueEventListener(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                                frienUser = dataSnapshot.getValue(Person.class);

                                                                firebaseStorage.getReference().child("images/"+ frienUser.getPhoto()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                    @Override
                                                                    public void onSuccess(Uri uri) {
                                                                        requestManager.load(uri.toString()).into(binding.rProfileImage);
                                                                    }
                                                                });

                                                                binding.setRecieveduser(frienUser);

                                                                if(frienUser.getStatus().equals("Online"))
                                                                {
                                                                    binding.statusImage.setImageResource(R.drawable.ic_online);

                                                                }else if(frienUser.getStatus().equals("Offline"))
                                                                {
                                                                    binding.statusImage.setImageResource(R.drawable.ic_busy);

                                                                }else if(frienUser.getStatus().equals("Away"))
                                                                {
                                                                    binding.statusImage.setImageResource(R.drawable.ic_away);
                                                                }

                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                                            }
                                                        });


                                                        if(conversationDataResource.data.getMessages().size() == 1
                                                                && userDataResource.data.getId().equals(((Message) conversationDataResource.data.getMessages().get("-1")).getSentBy().getId())
                                                        )
                                                        {
                                                            binding.bottomMessageSend.setVisibility(View.GONE);
                                                        }else
                                                        {
                                                            binding.bottomMessageSend.setVisibility(View.VISIBLE);
                                                        }


                                                        binding.chatMessages.setHasFixedSize(true);
                                                        // use a linear layout manager
                                                        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
                                                        linearLayoutManager.setReverseLayout(true);
                                                        binding.chatMessages.setLayoutManager(linearLayoutManager);

                                                        mViewModel.getMessagesAdapter(conversationId, frienUser,getViewLifecycleOwner()).observe(getViewLifecycleOwner(), new Observer<DataResource<FirebaseRecyclerPagingAdapter<Message, ChatAdapter.MyViewHolder>>>() {
                                                            @Override
                                                            public void onChanged(DataResource<FirebaseRecyclerPagingAdapter<Message, ChatAdapter.MyViewHolder>> firebaseRecyclerPagingAdapterDataResource) {
                                                                if(firebaseRecyclerPagingAdapterDataResource != null)
                                                                {
                                                                    switch (firebaseRecyclerPagingAdapterDataResource.status)
                                                                    {
                                                                        case SUCCESS:{
                                                                            firebaseRecyclerPagingAdapter = firebaseRecyclerPagingAdapterDataResource.data;
                                                                            break;
                                                                        }

                                                                        case LOADING:{
                                                                            binding.swipeRefreshLayout.setRefreshing(true);
                                                                            break;
                                                                        }

                                                                        case ERROR:{
                                                                            if(firebaseRecyclerPagingAdapterDataResource.message.equals("LOADED"))
                                                                            {
                                                                                binding.swipeRefreshLayout.setRefreshing(false);
                                                                            }else  if(firebaseRecyclerPagingAdapterDataResource.message.equals("FINISHED"))
                                                                            {
                                                                                binding.swipeRefreshLayout.setRefreshing(false);
                                                                            }

                                                                            break;
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        });

                                                        firebaseRecyclerPagingAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                                                            @Override
                                                            public void onItemRangeInserted(int positionStart, int itemCount) {
                                                                super.onItemRangeInserted(positionStart, itemCount);
                                                            }
                                                        });




                                                        binding.chatMessages.setAdapter(firebaseRecyclerPagingAdapter);


                                                        binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                                                            @Override
                                                            public void onRefresh() {
                                                                firebaseRecyclerPagingAdapter.refresh();
                                                            }
                                                        });

                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    });

                            break;
                        }
                    }
                }
            }
        });

        binding.messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().equals(""))
                {
                    binding.sendImage.setImageResource(R.drawable.ic_microphone);
                }else
                {
                    binding.sendImage.setImageResource(R.drawable.ic_send);
                }
            }
        });

        binding.sendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!binding.messageEditText.getText().equals(""))
                {
                    Long currentDate = Long.parseLong(new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date()));

                    final Message message = new Message(
                            lastMesssageIndex-1 + "",
                            "Text",
                            binding.messageEditText.getText().toString(),
                            "",
                            "",
                            "",
                            currentDate,
                            curentUserAsPerson,
                            "Sended"
                    );



                    mViewModel.sendTextMessage(message,frienUser,conversationId, lastMesssageIndex-1);
                    binding.messageEditText.setText("");
                }
            }
        });


        binding.emoji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager keyboard = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.showSoftInput(binding.messageEditText, 0);
            }
        });

        binding.attachFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        binding.signTranslation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Handler handler;
                Runnable runnable;
                if(binding.camera.getVisibility() == View.VISIBLE)
                {
                    binding.camera.setVisibility(View.GONE);
                    binding.camera.close();
                    handler = null;
                    runnable = null;
                }else{

                    binding.camera.open();
                    binding.camera.setVisibility(View.VISIBLE);
                    binding.camera.setLifecycleOwner(Chat.this);

                     handler = new Handler();
                     runnable = new Runnable() {
                        @Override
                        public void run() {
                            // binding.camera.setFilter(Filters.GRAYSCALE.newInstance());
                            binding.camera.setAudio(Audio.OFF);
                            binding.camera.takePicture();
                            handler.postDelayed(this, 2000);
                        }
                    };
                    handler.postDelayed(runnable, 2000);


                    binding.camera.addCameraListener(new CameraListener() {
                        @Override
                        public void onPictureTaken(final PictureResult result) {
                            result.toBitmap(40, 40, new BitmapCallback() {
                                @SuppressLint("WrongThread")
                                @Override
                                public void onBitmapReady(@Nullable Bitmap bitmap) {

                                    bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);
                                  if(bitmap != null)
                                  {
                                      final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);

                                      Log.d(TAG, "oraby onBitmapReady: " + results.toString());

                                      String message = "";

                                     for(int i=0; i<results.size();i++)
                                     {
                                         message +=  results.get(i).getTitle() + " ";
                                     }

                                      binding.messageEditText.setText(binding.messageEditText.getText() + message);
                                  }

                                }
                            });


                        }
                    });

                    initTensorFlowAndLoadModel();
                }
            }
        });


        binding.sendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle("nav_state", Navigation.findNavController(getActivity(), R.id.host_fragment).saveState());
        Log.e(TAG, "onSaveInstanceState: " +  Navigation.findNavController(getActivity(), R.id.host_fragment).saveState());
    }


    @Override
    public void onStart() {
        super.onStart();
        if(firebaseRecyclerPagingAdapter != null)
        {
            firebaseRecyclerPagingAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if(firebaseRecyclerPagingAdapter != null)
        {
            firebaseRecyclerPagingAdapter.stopListening();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.floatingActionButton_LiveTranslation.setVisibility(View.GONE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if(classifier != null)
                {classifier.close();}
            }
        });
    }

    private void initTensorFlowAndLoadModel() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier = TensorFlowImageClassifier.create(
                            getActivity().getAssets(),
                            MODEL_PATH,
                            LABEL_PATH,
                            INPUT_SIZE,
                            QUANT);
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }
}
