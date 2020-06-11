package com.example.murmuro.ui.main.people;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.murmuro.R;
import com.example.murmuro.databinding.PeopleFragmentBinding;
import com.example.murmuro.model.DataResource;
import com.example.murmuro.model.Message;
import com.example.murmuro.model.Person;
import com.example.murmuro.model.User;
import com.example.murmuro.storage.room.MurmuroRepositoryImp;
import com.example.murmuro.ui.main.MainActivity;
import com.example.murmuro.ui.main.chat.ChatAdapter;
import com.example.murmuro.ui.main.conversations.ConversationsDirections;
import com.example.murmuro.viewModel.ViewModelProviderFactory;
import com.shreyaspatil.firebase.recyclerpagination.FirebaseRecyclerPagingAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class People extends DaggerFragment {

    private PeopleViewModel mViewModel;
    private PeopleFragmentBinding binding;

    @Inject
    ViewModelProviderFactory providerFactory;


    FirebaseRecyclerPagingAdapter firebaseRecyclerPagingAdapter;


    public static People newInstance() { return new People(); }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.people_fragment, container, false);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.bottomNavigationView.setVisibility(View.VISIBLE);
        MainActivity.floatingActionButton_LiveTranslation.setVisibility(View.VISIBLE);
        if(firebaseRecyclerPagingAdapter != null)
        {
            firebaseRecyclerPagingAdapter.startListening();
            binding.peopleId.setAdapter(firebaseRecyclerPagingAdapter);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this, providerFactory).get(PeopleViewModel.class);

        mViewModel.setActivity(getActivity());
        if(savedInstanceState != null)
        {
            Navigation.findNavController(getActivity(), R.id.host_fragment).restoreState(savedInstanceState.getBundle("nav_state"));
            Log.e(TAG, "onRestoreInstanceState: " +  savedInstanceState.getBundle("nav_state").describeContents());
        }



        binding.peopleId.setHasFixedSize(true);
        // use a linear layout manager
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        binding.peopleId.setLayoutManager(linearLayoutManager);

        mViewModel.getPersonsAdapter(getViewLifecycleOwner()).observe(getViewLifecycleOwner(), new Observer<DataResource<FirebaseRecyclerPagingAdapter<Person, PeopleAdapter.MyViewHolder>>>() {
            @Override
            public void onChanged(DataResource<FirebaseRecyclerPagingAdapter<Person, PeopleAdapter.MyViewHolder>> firebaseRecyclerPagingAdapterDataResource) {
                if(firebaseRecyclerPagingAdapterDataResource != null)
                {
                    Log.e(TAG, "onChanged: firebaseRecyclerPagingAdapterDataResource is not  null" );
                    Log.e(TAG, "onChanged: firebaseRecyclerPagingAdapterDataResource status "  + firebaseRecyclerPagingAdapterDataResource.status);
                    switch (firebaseRecyclerPagingAdapterDataResource.status)
                    {
                        case SUCCESS:{
                            firebaseRecyclerPagingAdapter = firebaseRecyclerPagingAdapterDataResource.data;
                            binding.peopleId.setAdapter(firebaseRecyclerPagingAdapter);
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

}

