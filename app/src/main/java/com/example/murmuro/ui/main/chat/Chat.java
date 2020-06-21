package com.example.murmuro.ui.main.chat;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

import static android.app.Activity.RESULT_OK;
import static android.view.View.VISIBLE;
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

    private Uri filePath;

    private final int PICK_IMAGE_REQUEST = 71;
    private final int PICKFILE_RESULT_CODE = 72;
    private final int PICKVIDEO_RESULT_CODE = 73;
    private final int PICKRECORED_RESULT_CODE = 74;
    private final int PICKGIF_RESULT_CODE = 75;
    private final int REQ_CODE_SPEECH_INPUT = 76;

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
        mViewModel.setActivity(getActivity());
        mViewModel.setContext(getContext());
        if(savedInstanceState != null)
        {
            Navigation.findNavController(getActivity(), R.id.host_fragment).restoreState(savedInstanceState.getBundle("nav_state"));
            Log.e(TAG, "onRestoreInstanceState: " +  savedInstanceState.getBundle("nav_state").describeContents());
        }

        conversationId = ChatArgs.fromBundle(getArguments()).getConversationId();

        mViewModel.getCurrentUserDataResourceMutableLiveData().observe(getViewLifecycleOwner(), new Observer<DataResource<User>>() {
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
                                    .observe(getViewLifecycleOwner(), new Observer<DataResource<Conversation>>() {
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
                if(!binding.messageEditText.getText().toString().trim().equals(""))
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
                }else
                {
                    startVoiceInput();
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
                if( binding.mediaUpload.getVisibility() == VISIBLE)
                {
                    binding.mediaUpload.setVisibility(View.GONE);
                }else
                {
                    binding.mediaUpload.setVisibility(View.VISIBLE);
                }
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
                }else
                    {

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


        binding.attachImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        binding.fileUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseFile();
            }
        });

        binding.gifUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseGif();
            }
        });

        binding.recoredUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseRecored();
            }
        });

        binding.vidoUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseVideo();
            }
        });


        binding.backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(getActivity(), R.id.host_fragment).popBackStack();
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
    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hello, Say your message?");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {

        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void chooseFile() {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICKFILE_RESULT_CODE);
    }

    private void chooseRecored() {
        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICKRECORED_RESULT_CODE);
    }

    private void chooseGif() {
        Intent intent = new Intent();
        intent.setType("image/gif");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICKGIF_RESULT_CODE);
    }

    private void chooseVideo() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICKVIDEO_RESULT_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            Long currentDate = Long.parseLong(new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date()));

            filePath = data.getData();
            String filename = getFileName(data.getData());
            Message message = new Message(
                    lastMesssageIndex-1 + "",
                    "Photo",
                    filename,
                    "",
                    "",
                    filePath.toString(),
                    currentDate,
                    curentUserAsPerson,
                    "Sended"
            );

            mViewModel.sendStorageMessage(message, frienUser, conversationId, lastMesssageIndex-1);

        }else if(requestCode == PICKFILE_RESULT_CODE && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            Long currentDate = Long.parseLong(new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date()));
            filePath = data.getData();
            Log.e(TAG, "onActivityResult: " + filePath );

            String filename = getFileName(data.getData());

            Log.e(TAG, "onActivityResult:type  "  + getFileName(data.getData()));
            Log.e(TAG, "onActivityResult: filename " + filename );

            Message message = new Message(
                    lastMesssageIndex-1 + "",
                    "File",
                    filename,
                    "",
                    "",
                    filePath.toString(),
                    currentDate,
                    curentUserAsPerson,
                    "Sended"
            );

            mViewModel.sendStorageMessage(message, frienUser, conversationId, lastMesssageIndex-1);

        }else if(requestCode == PICKRECORED_RESULT_CODE && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            Long currentDate = Long.parseLong(new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date()));
            filePath = data.getData();
            Log.e(TAG, "onActivityResult: " + filePath );

            String filename = getFileName(data.getData());

            Log.e(TAG, "onActivityResult:type  "  + getFileName(data.getData()));
            Log.e(TAG, "onActivityResult: filename " + filename );

            Message message = new Message(
                    lastMesssageIndex-1 + "",
                    "Audio",
                    filename,
                    "",
                    "",
                    filePath.toString(),
                    currentDate,
                    curentUserAsPerson,
                    "Sended"
            );

            mViewModel.sendStorageMessage(message, frienUser, conversationId, lastMesssageIndex-1);

        }else if(requestCode == PICKGIF_RESULT_CODE && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            Long currentDate = Long.parseLong(new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date()));
            filePath = data.getData();
            Log.e(TAG, "onActivityResult: " + filePath );

            String filename = getFileName(data.getData());

            Log.e(TAG, "onActivityResult:type  "  + getFileName(data.getData()));
            Log.e(TAG, "onActivityResult: filename " + filename );

            Message message = new Message(
                    lastMesssageIndex-1 + "",
                    "Gif",
                    filename,
                    "",
                    "",
                    filePath.toString(),
                    currentDate,
                    curentUserAsPerson,
                    "Sended"
            );

            mViewModel.sendStorageMessage(message, frienUser, conversationId, lastMesssageIndex-1);

        }else if(requestCode == PICKVIDEO_RESULT_CODE && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            Long currentDate = Long.parseLong(new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date()));
            filePath = data.getData();
            Log.e(TAG, "onActivityResult: " + filePath );

            String filename = getFileName(data.getData());

            Log.e(TAG, "onActivityResult:type  "  + getFileName(data.getData()));
            Log.e(TAG, "onActivityResult: filename " + filename );

            Message message = new Message(
                    lastMesssageIndex-1 + "",
                    "Video",
                    filename,
                    "",
                    "",
                    filePath.toString(),
                    currentDate,
                    curentUserAsPerson,
                    "Sended"
            );

            mViewModel.sendStorageMessage(message, frienUser, conversationId, lastMesssageIndex-1);

        }else if(requestCode == REQ_CODE_SPEECH_INPUT && resultCode == RESULT_OK
                && data != null  )
        {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            binding.messageEditText.setText(result.get(0));
            Log.e(TAG, "onActivityResult: " +  (result.get(0)));
        }
        else
        {
            Log.e(TAG, "onActivityResult: "  + requestCode  );
        }
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
            Log.e(TAG, "getFileName: " + cursor.toString() );
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
}
