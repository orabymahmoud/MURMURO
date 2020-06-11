package com.example.murmuro.ui.auth.vervication;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.murmuro.R;
import com.example.murmuro.databinding.FragmentMobileNumberBinding;
import com.example.murmuro.ui.auth.createNew.CreateNewDirections;

import dagger.android.support.DaggerFragment;

public class MobileNumber extends DaggerFragment {

    private FragmentMobileNumberBinding binding;
    public MobileNumber() {
        // Required empty public constructor
    }

    public static MobileNumber newInstance() {
        return new MobileNumber();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       binding = DataBindingUtil.inflate(inflater, R.layout.fragment_mobile_number, container, false);
       binding.setNavigate(this);
        return binding.getRoot();
    }

    public void back()
    {
        Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(MobileNumberDirections.actionMobileNumberToCreateNew());
    }

    public void Confirm()
    {
        String phone = binding.mobileEt.getText().toString();
        String code =  binding.ccp.getSelectedCountryCode();
        String city = binding.ccp.getSelectedCountryName();

        Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(MobileNumberDirections.actionMobileNumberToConfirmation(
                phone,
                code,
                city,
                MobileNumberArgs.fromBundle(getArguments()).getEmail(),
                MobileNumberArgs.fromBundle(getArguments()).getUserName(),
                MobileNumberArgs.fromBundle(getArguments()).getPassword(),
                MobileNumberArgs.fromBundle(getArguments()).getName()
                ));
    }

}
