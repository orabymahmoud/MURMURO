package com.example.murmuro.ui.auth.createNew;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.murmuro.R;
import com.example.murmuro.databinding.CreateNewFragmentBinding;
import com.example.murmuro.ui.auth.createNew.sign_up_stepper.EmailStep;
import com.example.murmuro.ui.auth.createNew.sign_up_stepper.NameStep;
import com.example.murmuro.ui.auth.createNew.sign_up_stepper.PasswordStep;
import com.example.murmuro.ui.auth.createNew.sign_up_stepper.UserNameStep;
import com.example.murmuro.ui.auth.vervication.MobileNumber;
import com.example.murmuro.ui.auth.vervication.MobileNumberDirections;

import dagger.android.support.DaggerFragment;
import ernestoyaquello.com.verticalstepperform.listener.StepperFormListener;

public class CreateNew extends DaggerFragment implements StepperFormListener {

    private CreateNewViewModel mViewModel;
    private CreateNewFragmentBinding binding;
    private UserNameStep userNameStep;
    private EmailStep emailStep;
    private NameStep nameStep;
    private PasswordStep passwordStep;

    public static CreateNew newInstance() {
        return new CreateNew();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater ,R.layout.create_new_fragment, container, false);

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(CreateNewViewModel.class);

        if(savedInstanceState != null)
        {
            if(savedInstanceState.containsKey("user_name")) {
                String userName = savedInstanceState.getString("user_name");
                userNameStep.restoreStepData(userName);
            }

            if(savedInstanceState.containsKey("name")) {
                String name = savedInstanceState.getString("name");
                nameStep.restoreStepData(name);
            }

            if(savedInstanceState.containsKey("email")) {
                String email = savedInstanceState.getString("email");
                emailStep.restoreStepData(email);
            }

            if(savedInstanceState.containsKey("password")) {
                String password = savedInstanceState.getString("password");
                passwordStep.restoreStepData(password);
            }

        }

        binding.setNavigate(this);

        userNameStep = new UserNameStep("User Name");
        emailStep = new EmailStep("Email");
        passwordStep = new PasswordStep("Password");
        nameStep = new NameStep("Name");

        binding.stepperForm
                .setup(this,  nameStep , emailStep ,userNameStep , passwordStep)
                .allowNonLinearNavigation(true)
                .displayBottomNavigation(true)
                .displayCancelButtonInLastStep(true)
                .displayDifferentBackgroundColorOnDisabledElements(true)
                .displayStepButtons(true)
                .init();
    }

    @Override
    public void onCompletedForm() {

        Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(
                CreateNewDirections.actionCreateNewToMobileNumber(emailStep.getStepData(),
                        nameStep.getStepData(), userNameStep.getStepData(), passwordStep.getStepData()));
    }

    @Override
    public void onCancelledForm() {

        Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(CreateNewDirections.actionCreateNewToLogIn());
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("name", userNameStep.getStepData());
        outState.putString("user_name", userNameStep.getStepData());
        outState.putString("email", userNameStep.getStepData());
        outState.putString("password", userNameStep.getStepData());

        super.onSaveInstanceState(outState);
    }

    public void back()
    {
        Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(CreateNewDirections.actionCreateNewToLogIn());
    }
}