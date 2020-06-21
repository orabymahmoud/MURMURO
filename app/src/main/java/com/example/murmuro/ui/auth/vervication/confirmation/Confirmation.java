package com.example.murmuro.ui.auth.vervication.confirmation;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.example.murmuro.R;
import com.example.murmuro.databinding.ConfirmationFragmentBinding;
import com.example.murmuro.model.Conversation;
import com.example.murmuro.model.User;
import com.example.murmuro.model.AuthResource;
import com.example.murmuro.ui.auth.vervication.MobileNumberArgs;
import com.example.murmuro.ui.main.MainActivity;
import com.example.murmuro.viewModel.ViewModelProviderFactory;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;


public class Confirmation extends DaggerFragment {

    public static Confirmation newInstance() {
        return new Confirmation();
    }
    private String verificationid;
    private FirebaseAuth mAuth;
    private ConfirmationFragmentBinding binding;
    private ConfirmationViewModel mViewModel;
    private static final String TAG = "Confirmation";
    private CountDownTimer countDownTimer;
    @Inject
    ViewModelProviderFactory providerFactory;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.confirmation_fragment, container, false);
        return binding.getRoot();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel =  ViewModelProviders.of(this, providerFactory).get(ConfirmationViewModel.class);
        mViewModel.setAuthActivity(getActivity());
        binding.setNavigate(this);
        binding.progress.setVisibility(View.VISIBLE);

        String phone = ConfirmationArgs.fromBundle(getArguments()).getPhone();
        String code = ConfirmationArgs.fromBundle(getArguments()).getCode();


        binding.numberTxt.setText(getString(R.string.we_send_confirmation_code_in_small_messege_on_0123456789) + "+" + code + " " + phone );

        final String phonenumber = "+" + code + phone;
        timer();
        sendVerificationCode(phonenumber);
        binding.resendTx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerificationCode(phonenumber);
                timer();
                binding.progress.setVisibility(View.VISIBLE);
            }
        });

        binding.confirmBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                String code = editText.getText().toString().trim();
//
//                if ((code.isEmpty() || code.length() < 6)){
//
//                    editText.setError("Enter code...");
//                    editText.requestFocus();
//                    return;
//                }
//
//                try {
//                    progressBar.setVisibility(View.VISIBLE);
//                    verifyCode(code);
//                }catch (Exception e){
//                    Toast.makeText(VervicationActivity.this,getString(R.string.catch_vervication_error)+"" ,Toast.LENGTH_SHORT).show();
//                    progressBar.setVisibility(View.GONE);
//                    Log.e(TAG, "onClick: " + e );
//                }
            }
        });

    }

    private void timer(){

        binding.resendTx.setVisibility(View.GONE);
        countDownTimer = new CountDownTimer(120000, 1000) {
            public void onTick(long millisUntilFinished) {

                long timer = (millisUntilFinished / 1000) ;
                long  min = timer / 60;

                long sec = timer - (min * 60) ;

                binding.timerTx.setText(getString(R.string.wait_we_will_resend_code_agian_after)+ "\n " + String.format(min + "", "00") + ":" +String.format(sec + "", "00"));
            }

            public void onFinish() {
                binding.resendTx.setVisibility(View.VISIBLE);
                binding.progress.setVisibility(View.GONE);

            }
        };

        countDownTimer.start();
    }

    private void verifyCode(String code){
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationid, code);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            CreateNew();
                        } else {

                            binding.progress.setVisibility(View.GONE);
                            Log.e(TAG, "onComplete: vervication error " );
                            Toast.makeText(getContext(),"",Toast.LENGTH_SHORT).show();
                        }
                    }

                });


    }


    private void sendVerificationCode(String number){
        Toast.makeText(getContext(), getString(R.string.sending_code)+"",Toast.LENGTH_SHORT).show();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallBack
        );
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationid = s;
        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code != null){
                binding.progress.setVisibility(View.VISIBLE);
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {

            Toast.makeText(getContext(),"there is "  + e.getMessage(),Toast.LENGTH_LONG).show();
            Log.e(TAG, "onVerificationFailed: " + e.getMessage() );

        }
    };

    public void Back()
    {
        countDownTimer.cancel();
        Navigation.findNavController(getActivity(), R.id.nav_host_fragment).popBackStack();
    }

    public void CreateNew()
    {
        String phone = ConfirmationArgs.fromBundle(getArguments()).getPhone();
        String city = ConfirmationArgs.fromBundle(getArguments()).getCity();
        String name = ConfirmationArgs.fromBundle(getArguments()).getName();
        String username = ConfirmationArgs.fromBundle(getArguments()).getUserName();
        String email = ConfirmationArgs.fromBundle(getArguments()).getEmail();
        String password = ConfirmationArgs.fromBundle(getArguments()).getPassword();


        String bio = getString(R.string.bio_default);
        String photo = getString(R.string.profile_default);
        String status = getString(R.string.online);

        User user = new User("", name, username, password, photo, email, city, phone, bio, status, new HashMap<String, Conversation>());

        mViewModel.registerUser(user).observe(getActivity(), new Observer<AuthResource<User>>() {
            @Override
            public void onChanged(AuthResource<User> userAuthResource) {
                if(userAuthResource != null)
                {
                    switch (userAuthResource.status)
                    {
                        case ERROR:
                        {
                            binding.progress.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Error while register", Toast.LENGTH_SHORT).show();
                            break;
                        }

                        case LOADING:
                        {

                            break;
                        }

                        case AUTHENTICATED:
                        {
                            countDownTimer.cancel();
                            binding.progress.setVisibility(View.GONE);
                            Intent intent = new Intent(getContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            getActivity().finish();
                            break;
                        }

                        case NOT_AUTHENTICATED:{
                            binding.progress.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Not authenticated", Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                }else
                {
                    Log.e(TAG, "onChanged: " + "userAuthResource is null" );
                }
            }
        });

    }

}
