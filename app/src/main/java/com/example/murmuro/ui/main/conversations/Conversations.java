package com.example.murmuro.ui.main.conversations;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.RequestManager;
import com.example.murmuro.R;
import com.example.murmuro.databinding.ConversationsFragmentBinding;
import com.example.murmuro.model.Conversation;
import com.example.murmuro.model.DataResource;
import com.example.murmuro.model.Person;
import com.example.murmuro.model.User;
import com.example.murmuro.ui.main.MainActivity;
import com.example.murmuro.ui.main.groups.GroupsDirections;
import com.example.murmuro.viewModel.ViewModelProviderFactory;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.shreyaspatil.firebase.recyclerpagination.FirebaseRecyclerPagingAdapter;

import java.util.List;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class Conversations extends DaggerFragment {

    private ConversationsViewModel mViewModel;
    private ConversationsFragmentBinding binding;

    @Inject
    ViewModelProviderFactory providerFactory;

    @Inject
    FirebaseStorage firebaseStorage;

    @Inject
    RequestManager requestManager;


    FirebaseRecyclerPagingAdapter firebaseRecyclerPagingAdapter;

    public static Conversations newInstance() {
        return new Conversations();
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.bottomNavigationView.setVisibility(View.VISIBLE);
        MainActivity.floatingActionButton_LiveTranslation.setVisibility(View.VISIBLE);

        if(firebaseRecyclerPagingAdapter != null)
        {
            firebaseRecyclerPagingAdapter.startListening();
            binding.conversationId.setAdapter(firebaseRecyclerPagingAdapter);
        }

    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.conversations_fragment, container, false);

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this, providerFactory).get(ConversationsViewModel.class);
        mViewModel.setActivity(getActivity());

        if(savedInstanceState != null)
        {
            Navigation.findNavController(getActivity(), R.id.host_fragment).restoreState(savedInstanceState.getBundle("nav_state"));
            Log.e(TAG, "onRestoreInstanceState: " +  savedInstanceState.getBundle("nav_state").describeContents());
        }

        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Pacifico-Regular.ttf");
        binding.userName.setTypeface(typeface);



        binding.conversationId.setHasFixedSize(true);
        // use a linear layout manager
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        binding.conversationId.setLayoutManager(linearLayoutManager);

        mViewModel.getCurrentUserDataResourceMutableLiveData().observe(getActivity(), new Observer<DataResource<User>>() {
            @Override
            public void onChanged(DataResource<User> userDataResource) {
                if(userDataResource != null)
                {
                    switch (userDataResource.status)
                    {
                        case LOADING:{
                            binding.progressBar.setVisibility(View.VISIBLE);
                            break;
                        }

                        case ERROR:{
                            binding.progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), userDataResource.message, Toast.LENGTH_SHORT).show();
                            break;
                        }

                        case SUCCESS:{
                            binding.progressBar.setVisibility(View.GONE);
                            binding.setUser(userDataResource.data);

                            firebaseStorage.getReference().child("images/"+ userDataResource.data.getPhoto()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    requestManager.load(uri.toString()).into(binding.profileImage);
                                }
                            });

                            mViewModel.getConversationAdapter(userDataResource.data, getViewLifecycleOwner()).observe(getViewLifecycleOwner(), new Observer<DataResource<FirebaseRecyclerPagingAdapter<Conversation, ConversationAdapter.MyViewHolder>>>() {
                                @Override
                                public void onChanged(DataResource<FirebaseRecyclerPagingAdapter<Conversation, ConversationAdapter.MyViewHolder>> firebaseRecyclerPagingAdapterDataResource) {
                                    if(firebaseRecyclerPagingAdapterDataResource != null)
                                    {
                                        Log.e(TAG, "onChanged: firebaseRecyclerPagingAdapterDataResource is not  null" );
                                        Log.e(TAG, "onChanged: firebaseRecyclerPagingAdapterDataResource status "  + firebaseRecyclerPagingAdapterDataResource.status);
                                        switch (firebaseRecyclerPagingAdapterDataResource.status)
                                        {
                                            case SUCCESS:{
                                                firebaseRecyclerPagingAdapter = firebaseRecyclerPagingAdapterDataResource.data;
                                                binding.conversationId.setAdapter(firebaseRecyclerPagingAdapter);
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
                                                    Log.e(TAG, "firebaseRecyclerPagingAdapterDataResource: " + "LOADED");


                                                }else  if(firebaseRecyclerPagingAdapterDataResource.message.equals("FINISHED"))
                                                {
                                                    binding.swipeRefreshLayout.setRefreshing(false);
                                                    Log.e(TAG, "firebaseRecyclerPagingAdapterDataResource: " + "FINISHED" );

                                                }else
                                                {
                                                    binding.swipeRefreshLayout.setRefreshing(false);
                                                    Log.e(TAG, "firebaseRecyclerPagingAdapterDataResource: " + firebaseRecyclerPagingAdapterDataResource.message );
                                                }

                                                break;
                                            }
                                        }
                                    }else {
                                        Log.e(TAG, "onChanged: firebaseRecyclerPagingAdapterDataResource is null" );
                                    }


                                }
                            });

                            break;
                        }
                    }
                }
            }
        });

        if(firebaseRecyclerPagingAdapter != null)
        {
            firebaseRecyclerPagingAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    super.onItemRangeInserted(positionStart, itemCount);
                }
            });
        }

        binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                firebaseRecyclerPagingAdapter.refresh();
            }
        });

        binding.conversationId.setAdapter(firebaseRecyclerPagingAdapter);


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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle("nav_state", Navigation.findNavController(getActivity(), R.id.host_fragment).saveState());
        Log.e(TAG, "onSaveInstanceState: " +  Navigation.findNavController(getActivity(), R.id.host_fragment).saveState());
    }


}


