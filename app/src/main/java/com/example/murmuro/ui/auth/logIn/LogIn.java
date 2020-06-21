package com.example.murmuro.ui.auth.logIn;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.murmuro.R;
import com.example.murmuro.databinding.LogInFragmentBinding;
import com.example.murmuro.model.User;
import com.example.murmuro.model.AuthResource;
import com.example.murmuro.ui.main.MainActivity;
import com.example.murmuro.viewModel.ViewModelProviderFactory;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

public class LogIn extends DaggerFragment {

    private LogInViewModel mViewModel;
    private LogInFragmentBinding binding;
    private static final String TAG = "LogIn";

    @Inject
    ViewModelProviderFactory providerFactory;

    public static LogIn newInstance() {
        return new LogIn();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater,R.layout.log_in_fragment, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel =  ViewModelProviders.of(this, providerFactory).get(LogInViewModel.class);
        // TODO: Use the ViewModel
        binding.setNavigate(this);
        mViewModel.setAuthActivity(getActivity());
        binding.loginBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ObserveLogIn();


            }
        });

        binding.skipBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "onClick: "  );
                Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(LogInDirections.actionLogInToLiveTranslation3());

            }
        });

    }

    public void ObserveLogIn(){

        String username = binding.usernameEt.getText().toString();
        String password = binding.passwordEt.getText().toString();
        String pattern = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}";

        if(TextUtils.isEmpty(username) && android.util.Patterns.EMAIL_ADDRESS.matcher(username).matches())
        {
            binding.usernameEt.setError("Invalid User name");
            return;
        }

        if(!password.matches(pattern))
        {
            binding.passwordEt.setError("Invalid Password");
            return;
        }
        binding.progressBar.setVisibility(View.VISIBLE);

        mViewModel.LogIn(username, password).observe(this, new Observer<AuthResource<User>>() {
            @Override
            public void onChanged(AuthResource<User> userAuthResource) {
                if(userAuthResource != null)
                {
                    switch (userAuthResource.status)
                    {
                        case NOT_AUTHENTICATED:
                        {
                            binding.progressBar.setVisibility(View.GONE);
                        }

                        case AUTHENTICATED:
                        {
                            binding.progressBar.setVisibility(View.GONE);
                            Intent intent = new Intent(getContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            getActivity().finish();
                        }

                        case LOADING:
                        {
                            binding.progressBar.setVisibility(View.VISIBLE);
                        }

                        case ERROR:
                        {
                            binding.progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), userAuthResource.message , Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });




    }

    public void goToCreateNew()
    {
        Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(LogInDirections.actionLogInToCreateNew());
    }

}
