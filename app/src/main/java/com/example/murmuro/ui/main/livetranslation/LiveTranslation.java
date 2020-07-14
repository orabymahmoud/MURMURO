package com.example.murmuro.ui.main.livetranslation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.RequestManager;
import com.example.murmuro.R;
import com.example.murmuro.Utils;
import com.example.murmuro.databinding.LiveTranslationFragmentBinding;
import com.example.murmuro.machineLearning.Classifier;
import com.example.murmuro.machineLearning.TensorFlowImageClassifier;
import com.example.murmuro.model.Message;
import com.example.murmuro.ui.main.chat.Chat;
import com.example.murmuro.viewModel.ViewModelProviderFactory;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.otaliastudios.cameraview.BitmapCallback;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.controls.Audio;
import com.otaliastudios.cameraview.controls.Flash;
import com.otaliastudios.cameraview.controls.Mode;
import com.otaliastudios.cameraview.filter.Filters;

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
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static androidx.constraintlayout.widget.Constraints.TAG;

public class LiveTranslation extends DaggerFragment {

    private LiveTranslationViewModel mViewModel;
    private LiveTranslationFragmentBinding binding;
    private Handler handler;
    private  Runnable runnable;
    private Handler wordsHandler ;
    private Runnable wordsRunnable ;
    private int wordsIndex = 0;
    private final int REQ_CODE_SPEECH_INPUT = 76;

    @Inject
    ViewModelProviderFactory providerFactory;
    @Inject
    FirebaseStorage firebaseStorage;
    @Inject
    RequestManager requestManager;

    private static final String MODEL_PATH = "model.tflite";
    private static final boolean QUANT = true;
    private static final String LABEL_PATH = "Labels.txt";
    private static final int INPUT_SIZE = 40;
    private Classifier classifier;
    private Executor executor = Executors.newSingleThreadExecutor();
    private TextToSpeech t1;

    public static LiveTranslation newInstance() {
        return new LiveTranslation();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.live_translation_fragment, container, false);
        ((AppCompatActivity)getActivity()).setSupportActionBar(binding.toolbar);
        setHasOptionsMenu(true);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this,providerFactory).get(LiveTranslationViewModel.class);
        if(savedInstanceState != null)
        {
            try {
                Navigation.findNavController(getActivity(), R.id.host_fragment).restoreState(savedInstanceState.getBundle("nav_state"));
                Log.e(TAG, "onRestoreInstanceState: " +  savedInstanceState.getBundle("nav_state").describeContents());

            }catch (Exception e)
            {}


        }

        binding.camera.setLifecycleOwner(this);

          handler = new Handler();
         runnable = new Runnable() {
            @Override
            public void run() {
               // binding.camera.setFilter(Filters.GRAYSCALE.newInstance());
                binding.camera.setAudio(Audio.OFF);
                binding.camera.takePicture();
                if(handler!= null && runnable != null)
                {
                    handler.postDelayed(this, 2000);
                }
            }
        };
        if(handler!= null && runnable != null)
        {
            handler.postDelayed(runnable, 2000);
        }


        binding.camera.close();


        binding.camera.addCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(final PictureResult result) {
                result.toBitmap(40, 40, new BitmapCallback() {
                    @SuppressLint("WrongThread")
                    @Override
                    public void onBitmapReady(@Nullable Bitmap bitmap) {

                        bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);
                        final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);

                        Log.d(TAG, "oraby onBitmapReady: " + results.toString());
                        String message = "";

                        for(int i=0; i<results.size();i++)
                        {
                            message +=  results.get(i).getTitle() + " ";
                        }
                        binding.translatedText.setText(binding.translatedText.getText() + message);

                    }
                });


            }
        });

        initTensorFlowAndLoadModel();


        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(getActivity(), R.id.host_fragment).popBackStack();
            }
        });


        binding.avtarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.camera.setVisibility(View.GONE);
                binding.camera.close();
                handler = null;
                runnable = null;

                binding.avtarLayout.setVisibility(View.VISIBLE);
                binding.editTextLayout.setVisibility(View.VISIBLE);
                binding.signTranslationButton.setVisibility(View.VISIBLE);
                binding.translatedTextLayout.setVisibility(View.GONE);
                binding.avtarButton.setVisibility(View.GONE);

                if(!mViewModel.isInternetAvailable())
                {
                    binding.runningSign.setText("Check Internet Connection");
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

        firebaseStorage.getReference().child("Signs/" + "resetPose" + ".gif").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                requestManager.asGif().load(uri.toString()).into(binding.avtarImageView);

            }
        });

        binding.sendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!binding.messageEditText.getText().toString().trim().equals(""))
                {
                    String text = binding.messageEditText.getText().toString().toLowerCase().trim() + " resetPose ";
                    text=text.replaceAll("[^a-zA-Z ]", " ");

                    final List<String> words = new ArrayList<>();
                    String word = "";

                    Log.e(TAG, "onClick: " + text );

                    for(int i=0; i < text.length();i++)
                    {
                        if(text.charAt(i) == ' ')
                        {
                            Log.e(TAG, "onClick: Utils.AVTAR_SIGNS.contains(word.toLowerCase())" + Utils.AVTAR_SIGNS.contains(word.toLowerCase()) );

                            if(!word.equals(""))
                            {
                                if(Utils.AVTAR_SIGNS.contains(word.toLowerCase()))
                                {
                                    words.add(word);
                                    Log.e(TAG, "onClick: add " + word );
                                }else{

                                    for(int j=0; j < word.length();j++)
                                    {
                                        words.add(word.charAt(j) + "");
                                        Log.e(TAG, "onClick: add " + word.charAt(j) );
                                    }
                                }

                            }

                            word = "";
                        }else
                        {
                            word += text.charAt(i);
                        }
                    }



                    Log.e(TAG, "onClick: wordsIndex " + wordsIndex );
                    wordsHandler = new Handler();
                    wordsRunnable = new Runnable() {
                        @Override
                        public void run() {
                            if(wordsIndex < words.size())
                            {

                                firebaseStorage.getReference().child("Signs/" + words.get(wordsIndex) + ".gif").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        requestManager.asGif().load(uri.toString()).into(binding.avtarImageView);
                                        if(wordsIndex < words.size())
                                        {
                                            if(wordsIndex < words.size()-1)
                                            {
                                                binding.runningSign.setText(words.get(wordsIndex));
                                            }
                                            Log.e(TAG, "onSuccess: loaded a " + words.get(wordsIndex) );
                                            wordsIndex++;

                                        }

                                    }
                                });

                            }else
                            {
                                wordsIndex = 0;
                                wordsHandler = null;
                                wordsRunnable = null;
                                binding.runningSign.setText("");

                            }

                            if(wordsHandler != null || wordsRunnable != null)
                            {
                                wordsHandler.postDelayed(this, 3000);
                            }
                        }
                    };
                    if(wordsHandler != null || wordsRunnable != null)
                    {
                        wordsHandler.postDelayed(wordsRunnable, 3000);
                    }

                }else
                {
                    startVoiceInput();
                }
            }
        });


        binding.signTranslationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                binding.avtarLayout.setVisibility(View.GONE);
                binding.editTextLayout.setVisibility(View.GONE);
                binding.signTranslationButton.setVisibility(View.GONE);
                binding.translatedTextLayout.setVisibility(View.VISIBLE);
                binding.avtarButton.setVisibility(View.VISIBLE);



                binding.camera.open();
                binding.camera.setVisibility(View.VISIBLE);
                binding.camera.setAudio(Audio.OFF);
                binding.camera.setFlash(Flash.OFF);
                binding.camera.setMode(Mode.PICTURE);
                binding.camera.setPlaySounds(false);
                binding.camera.setLifecycleOwner(LiveTranslation.this);

                handler = new Handler();
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        // binding.camera.setFilter(Filters.GRAYSCALE.newInstance());
                        binding.camera.setAudio(Audio.OFF);
                        binding.camera.setFlash(Flash.OFF);
                        binding.camera.takePicture();

                        if(handler!= null && runnable != null) {
                            handler.postDelayed(this, 2);
                        }
                    }
                };
                if(handler!= null && runnable != null) {
                    handler.postDelayed(runnable, 2);
                }


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
                                    final List<Classifier.Recognition> results = classifier.recognizeImage(toGrayscale(bitmap));


                                    Log.e(TAG, "oraby onBitmapReady: " +  results);

                                    String message = "";

                                    for(int i=0; i<results.size();i++)
                                    {
                                        message +=  results.get(i) + " ";
                                    }

                                    binding.messageEditText.setText(binding.messageEditText.getText() + message);
                                }

                            }
                        });


                    }
                });

                initTensorFlowAndLoadModel();
            }
        });

        t1=new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });

        binding.textToSpeech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                t1.speak(binding.translatedText.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);

            }
        });

    }

    public Bitmap toGrayscale(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();
        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

           try {
               outState.putBundle("nav_state", Navigation.findNavController(getActivity(), R.id.host_fragment).saveState());
               Log.e(TAG, "onSaveInstanceState: " +  Navigation.findNavController(getActivity(), R.id.host_fragment).saveState());

           }catch (Exception e)
           {}
    }
    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                classifier.close();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQ_CODE_SPEECH_INPUT && resultCode == RESULT_OK
                && data != null  )
        {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            binding.messageEditText.setText(result.get(0));
            Log.e(TAG, "onActivityResult: " +  (result.get(0)));


        }
    }
}
