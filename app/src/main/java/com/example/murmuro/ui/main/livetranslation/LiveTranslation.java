package com.example.murmuro.ui.main.livetranslation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.murmuro.R;
import com.example.murmuro.databinding.LiveTranslationFragmentBinding;
import com.example.murmuro.machineLearning.Classifier;
import com.example.murmuro.machineLearning.TensorFlowImageClassifier;
import com.example.murmuro.viewModel.ViewModelProviderFactory;
import com.otaliastudios.cameraview.BitmapCallback;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.controls.Audio;
import com.otaliastudios.cameraview.filter.Filters;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class LiveTranslation extends DaggerFragment {

    private LiveTranslationViewModel mViewModel;
    private LiveTranslationFragmentBinding binding;

    @Inject
    ViewModelProviderFactory providerFactory;

    private static final String MODEL_PATH = "model.tflite";
    private static final boolean QUANT = true;
    private static final String LABEL_PATH = "Labels.txt";
    private static final int INPUT_SIZE = 40;
    private Classifier classifier;
    private Executor executor = Executors.newSingleThreadExecutor();

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
            Navigation.findNavController(getActivity(), R.id.host_fragment).restoreState(savedInstanceState.getBundle("nav_state"));
            Log.e(TAG, "onRestoreInstanceState: " +  savedInstanceState.getBundle("nav_state").describeContents());
        }

        binding.camera.setLifecycleOwner(this);

        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
               // binding.camera.setFilter(Filters.GRAYSCALE.newInstance());
                binding.camera.setAudio(Audio.OFF);
                binding.camera.takePicture();
                handler.postDelayed(this, 2000);
            }
        };
        handler.postDelayed(runnable, 2000);

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

    }



    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle("nav_state", Navigation.findNavController(getActivity(), R.id.host_fragment).saveState());
        Log.e(TAG, "onSaveInstanceState: " +  Navigation.findNavController(getActivity(), R.id.host_fragment).saveState());
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

}
