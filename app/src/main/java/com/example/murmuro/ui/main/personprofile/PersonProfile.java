package com.example.murmuro.ui.main.personprofile;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.RequestManager;
import com.example.murmuro.R;
import com.example.murmuro.databinding.PersonProfileFragmentBinding;
import com.example.murmuro.model.Conversation;
import com.example.murmuro.model.DataResource;
import com.example.murmuro.model.Person;
import com.example.murmuro.model.User;
import com.example.murmuro.ui.main.MainActivity;
import com.example.murmuro.viewModel.ViewModelProviderFactory;
import com.github.abdularis.civ.CircleImageView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class PersonProfile extends DaggerFragment {

    private PersonProfileViewModel mViewModel;
    @Inject
    ViewModelProviderFactory providerFactory;

    @Inject
    RequestManager requestManager;

    @Inject
    FirebaseStorage firebaseStorage;



    private PersonProfileFragmentBinding binding;

    public static PersonProfile newInstance() {
        return new PersonProfile();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater,R.layout.person_profile_fragment, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this, providerFactory).get(PersonProfileViewModel.class);

        if(savedInstanceState != null)
        {
            Navigation.findNavController(getActivity(), R.id.host_fragment).restoreState(savedInstanceState.getBundle("nav_state"));
            Log.e(TAG, "onRestoreInstanceState: " +  savedInstanceState.getBundle("nav_state").describeContents());
        }


        final Person person = new Person(
                PersonProfileArgs.fromBundle(getArguments()).getId(),
                PersonProfileArgs.fromBundle(getArguments()).getName(),
                PersonProfileArgs.fromBundle(getArguments()).getPhoto(),
                PersonProfileArgs.fromBundle(getArguments()).getBio(),
                PersonProfileArgs.fromBundle(getArguments()).getStatus(),
                PersonProfileArgs.fromBundle(getArguments()).getEmail(),
                PersonProfileArgs.fromBundle(getArguments()).getCity(),
                PersonProfileArgs.fromBundle(getArguments()).getPhone()
        );

        binding.setPerson(person);
        binding.setNavigate(this);

        CircleImageView statuscircleImageView = binding.statusImage;
        final CircleImageView profilecircleImageView =  binding.profileImage;

        firebaseStorage.getReference().child("images/"+ person.getPhoto()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                requestManager.load(uri.toString()).into(profilecircleImageView);
            }
        });

        if(person.getStatus().equals("Online"))
        {
            statuscircleImageView.setImageResource(R.drawable.ic_online);

        }else if(person.getStatus().equals("Offline"))
        {
            statuscircleImageView.setImageResource(R.drawable.ic_busy);

        }else if(person.getStatus().equals("Away"))
        {
            statuscircleImageView.setImageResource(R.drawable.ic_away);
        }



        binding.chatFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.progressBar.setVisibility(View.VISIBLE);
                mViewModel.getConversationId(person).observe(getActivity(), new Observer<DataResource<Conversation>>() {
                    @Override
                    public void onChanged(DataResource<Conversation> stringDataResource) {
                        if(stringDataResource != null)
                        {

                            switch (stringDataResource.status)
                            {
                                case LOADING:
                                {
                                    binding.progressBar.setVisibility(View.VISIBLE);
                                    break;
                                }

                                case ERROR:
                                {
                                    binding.progressBar.setVisibility(View.GONE);
                                    Toast.makeText(getActivity() , stringDataResource.message , Toast.LENGTH_SHORT).show();
                                    break;
                                }

                                case SUCCESS:
                                {
                                    binding.progressBar.setVisibility(View.GONE);
                                    Navigation.findNavController(getActivity(), R.id.host_fragment)
                                            .navigate(PersonProfileDirections.actionPersonProfileToChat(stringDataResource.data.getId()));
                                    break;
                                }
                            }

                        }
                    }
                });

            }
        });


    }

    public void back(){


    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle("nav_state", Navigation.findNavController(getActivity(), R.id.host_fragment).saveState());
        Log.e(TAG, "onSaveInstanceState: " +  Navigation.findNavController(getActivity(), R.id.host_fragment).saveState());
    }


}
