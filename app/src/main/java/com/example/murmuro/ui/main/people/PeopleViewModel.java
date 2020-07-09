package com.example.murmuro.ui.main.people;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.navigation.Navigation;
import androidx.paging.PagedList;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.Resource;
import com.example.murmuro.R;
import com.example.murmuro.model.Conversation;
import com.example.murmuro.model.DataResource;
import com.example.murmuro.model.Message;
import com.example.murmuro.model.Person;
import com.example.murmuro.storage.room.MurmuroRepositoryImp;
import com.example.murmuro.ui.main.MainActivity;
import com.example.murmuro.ui.main.chat.ChatAdapter;
import com.github.abdularis.civ.CircleImageView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.shreyaspatil.firebase.recyclerpagination.DatabasePagingOptions;
import com.shreyaspatil.firebase.recyclerpagination.FirebaseRecyclerPagingAdapter;
import com.shreyaspatil.firebase.recyclerpagination.LoadingState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static android.view.View.GONE;
import static androidx.constraintlayout.widget.Constraints.TAG;

public class PeopleViewModel extends ViewModel {

    private static final String TAG = "PeopleViewModel";
    private MurmuroRepositoryImp murmuroRepositoryImp;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseStorage firebaseStorage;
    private FirebaseAuth firebaseAuth;
    private Activity activity;
    private RequestManager requestManager;
    private MutableLiveData<DataResource<List<Person>>> personsMutableLiveData = new MutableLiveData<>();
    private MutableLiveData<DataResource<FirebaseRecyclerPagingAdapter<Person, PeopleAdapter.MyViewHolder>>> peopleAdapterDataResourceMutableLiveData = new MutableLiveData<>();

    @Inject
    public PeopleViewModel(MurmuroRepositoryImp murmuroRepositoryImp, FirebaseDatabase firebaseDatabase,
                           FirebaseAuth firebaseAuth, FirebaseStorage firebaseStorage, RequestManager requestManager) {
        this.murmuroRepositoryImp = murmuroRepositoryImp;
        this.firebaseDatabase = firebaseDatabase;
        this.firebaseAuth = firebaseAuth;
        this.firebaseStorage = firebaseStorage;
        this.requestManager = requestManager;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public MutableLiveData<DataResource<List<Person>>> getPersonsMutableLiveData() {
        return personsMutableLiveData;
    }

    public MutableLiveData<DataResource<FirebaseRecyclerPagingAdapter<Person, PeopleAdapter.MyViewHolder>>>
    getPersonsAdapter(LifecycleOwner lifecycleOwner, final String searchQuety)
    {
        final List<Person> people = new ArrayList<>();

        if(isInternetAvailable())
        {
            DatabaseReference userDatabaseReference = firebaseDatabase.getReference("users");

            PagedList.Config config = new PagedList.Config.Builder()
                    .setEnablePlaceholders(false)
                    .setPrefetchDistance(5)
                    .setPageSize(10)
                    .build();

            DatabasePagingOptions<Person> options = new DatabasePagingOptions.Builder<Person>()
                    .setLifecycleOwner(lifecycleOwner)
                    .setQuery(userDatabaseReference , config, Person.class)
                    .build();

            murmuroRepositoryImp.deleteAllPersons();

            FirebaseRecyclerPagingAdapter<Person, PeopleAdapter.MyViewHolder>  firebaseRecyclerPagingAdapter
                    = new FirebaseRecyclerPagingAdapter<Person, PeopleAdapter.MyViewHolder>(options) {
                @Override
                protected void onBindViewHolder(@NonNull PeopleAdapter.MyViewHolder myViewHolder, int i, @NonNull final Person person) {

                    Log.e(TAG, "onBindViewHolder: " + person.getName().toLowerCase() );
                    Log.e(TAG, "onBindViewHolder: " +  searchQuety.toLowerCase());

                    if(!searchQuety.equals(""))
                    {
                        if(person.getName().toLowerCase().contains(searchQuety.toLowerCase()) )
                        {
                            myViewHolder.bind(person);
                            people.add(person);
                        }else
                        {
                            myViewHolder.itemView.setVisibility(GONE);
                        }

                    }else
                    {
                        myViewHolder.bind(person);
                        people.add(person);
                    }


                      myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                          @Override
                          public void onClick(View v) {
                              Navigation.findNavController(activity, R.id.host_fragment).navigate(PeopleDirections.actionPoepleToPersonProfile(
                                      person.getName(),
                                      person.getBio(),
                                      person.getPhoto(),
                                      person.getStatus(),
                                      person.getMobile(),
                                      person.getEmail(),
                                      person.getCity(),
                                      person.getId()
                              ));
                              MainActivity.bottomNavigationView.setVisibility(View.GONE);
                              MainActivity.floatingActionButton_LiveTranslation.setVisibility(View.GONE);
                          }
                      });


                      Log.e(TAG, "onBindViewHolder: " + person.getBio());
                      murmuroRepositoryImp.insertPerson(person);

                      CircleImageView statuscircleImageView =  myViewHolder.itemView.findViewById(R.id.status_color);
                      final CircleImageView profilecircleImageView =  myViewHolder.itemView.findViewById(R.id.people_image);

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

                      firebaseStorage.getReference().child("images/"+ person.getPhoto()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                          @Override
                          public void onSuccess(Uri uri) {
                              requestManager.load(uri.toString()).into(profilecircleImageView);
                          }
                      });

                }


                @Override
                protected void onLoadingStateChanged(@NonNull LoadingState state) {
                    switch (state) {
                        case LOADING_INITIAL:
                        case LOADING_MORE:
                            // Do your loading animation
                            peopleAdapterDataResourceMutableLiveData.setValue(DataResource.loading((FirebaseRecyclerPagingAdapter<Person, PeopleAdapter.MyViewHolder>)null));
                            break;

                        case LOADED:
                            // Stop Animation
                            if(personsMutableLiveData != null)
                            {
                                personsMutableLiveData.setValue(DataResource.success(people));
                            }

                            if(people != null)
                            {
                                people.clear();
                            }
                            peopleAdapterDataResourceMutableLiveData.setValue(DataResource.error("LOADED" , (FirebaseRecyclerPagingAdapter<Person, PeopleAdapter.MyViewHolder>)null));
                            break;

                        case FINISHED:
                            //Reached end of Data set
                            peopleAdapterDataResourceMutableLiveData.setValue(DataResource.error("FINISHED" , (FirebaseRecyclerPagingAdapter<Person, PeopleAdapter.MyViewHolder>)null));
                            if(personsMutableLiveData != null)
                            {
                                personsMutableLiveData.setValue(DataResource.error("" , (List<Person>) people));
                            }

                            break;

                        case ERROR:
                            retry();
                            break;
                    }
                }

                @NonNull
                @Override
                public PeopleAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
                    ViewDataBinding binding = DataBindingUtil.inflate(layoutInflater, R.layout.people_item, parent, false);
                    return new PeopleAdapter.MyViewHolder(binding);
                }

                @Override
                protected void onError(@NonNull DatabaseError databaseError) {
                    super.onError(databaseError);
                    databaseError.toException().printStackTrace();
                    peopleAdapterDataResourceMutableLiveData.setValue(DataResource.error(databaseError.getMessage() , (FirebaseRecyclerPagingAdapter<Person, PeopleAdapter.MyViewHolder>)null));
                }
            };

            peopleAdapterDataResourceMutableLiveData.setValue(DataResource.success(firebaseRecyclerPagingAdapter));
            Log.e(TAG, "getPersonsAdapter: online " + isInternetAvailable() );

        }else{
            Log.e(TAG, "getPersonsAdapter: offline  " + isInternetAvailable() );
            final List<Person> offLinepersonList = new ArrayList<>();
            murmuroRepositoryImp.getPersons()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .onErrorReturn(new Function<Throwable, List<Person>>() {
                        @Override
                        public List<Person> apply(Throwable throwable) throws Exception {
                            List list = new LinkedList();
                            Person person = new Person();
                            person.setId("-1");
                            return list;
                        }
                    })
                    .map(new Function<List<Person>, Object>() {
                        @Override
                        public Object apply(List<Person> people) throws Exception {
                            if(people.get(0).getId().equals("-1"))
                            {
                              //  resourceMutableLiveData.setValue(DataResource.error("Can not load People", (List<Person>) null));
                                return null;
                            }

                            if(people != null)
                            {
                                for(int i=0; i<people.size(); i++){
                                    offLinepersonList.add(people.get(i));
                                }
                            }

                            Log.e(TAG, "getPeople: " + offLinepersonList.size() );
                           // resourceMutableLiveData.setValue(DataResource.success(offLinepersonList));

                            return people;
                        }
                    }).subscribe();
        }

        return peopleAdapterDataResourceMutableLiveData;
    }

    private boolean isInternetAvailable() {
        try {
            final String command = "ping -c 1 google.com";
            return Runtime.getRuntime().exec(command).waitFor() == 0;
        } catch (Exception e) {
            Log.e(TAG, "ProvidesisInternetAvailable: " + e.getMessage());
            return false;
        }
    }

}
