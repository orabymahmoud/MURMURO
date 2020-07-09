package com.example.murmuro.ui.main.conversations;

import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.navigation.Navigation;
import androidx.paging.PagedList;

import com.bumptech.glide.RequestManager;
import com.example.murmuro.R;
import com.example.murmuro.model.Conversation;
import com.example.murmuro.model.DataResource;
import com.example.murmuro.model.Person;
import com.example.murmuro.model.User;
import com.example.murmuro.storage.room.MurmuroRepositoryImp;
import com.example.murmuro.ui.main.MainActivity;
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
import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static android.view.View.GONE;

public class ConversationsViewModel extends ViewModel {


    private static final String TAG = "PeopleViewModel";
    private MurmuroRepositoryImp murmuroRepositoryImp;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    private FirebaseStorage firebaseStorage;
    private RequestManager requestManager;
    private Activity activity;
    private MutableLiveData<DataResource<List<Conversation>>> conversations =  new MutableLiveData<>();;
    private MutableLiveData<DataResource<User>> currentUserDataResourceMutableLiveData = new MutableLiveData<>();
    private MutableLiveData<DataResource<FirebaseRecyclerPagingAdapter<Conversation, ConversationAdapter.MyViewHolder>>> conversationAdapterDataResourceMutableLiveData = new MutableLiveData<>();


    @Inject
    public ConversationsViewModel(MurmuroRepositoryImp murmuroRepositoryImp, FirebaseDatabase firebaseDatabase, FirebaseAuth firebaseAuth,FirebaseStorage firebaseStorage,RequestManager requestManager) {
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

    public MutableLiveData<DataResource<User>> getCurrentUserDataResourceMutableLiveData()
    {

            currentUserDataResourceMutableLiveData.setValue(DataResource.loading((User) null));

            murmuroRepositoryImp.getUserById(firebaseAuth.getCurrentUser().getUid())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .onErrorReturn(new Function<Throwable, User>() {
                        @Override
                        public User apply(Throwable throwable) throws Exception {
                            User user = new User();
                            user.setId("-1");
                            return user;
                        }
                    }).map(new Function<User, Object>() {
                @Override
                public Object apply(User user) throws Exception {
                    if(user.getId().equals("-1"))
                    {
                        currentUserDataResourceMutableLiveData.setValue(DataResource.error("can not load user" , (User) null));
                       return null;
                    }

                     currentUserDataResourceMutableLiveData.setValue(DataResource.success(user));
                    return user;
                }
            }).subscribe();


        return currentUserDataResourceMutableLiveData;
    }

    public MutableLiveData<DataResource<List<Conversation>>> getConversations() {
        return conversations;
    }

    public MutableLiveData<DataResource<FirebaseRecyclerPagingAdapter<Conversation, ConversationAdapter.MyViewHolder>>>
    getConversationAdapter(User user, LifecycleOwner lifecycleOwner, final String searchQuety){
        final List<Conversation> conversationList = new ArrayList<>();

     if(isInternetAvailable())
     {
         DatabaseReference conversationDatabaseReference = firebaseDatabase.
                 getReference("users").
                 child(user.getId()).
                 child("conversations");

         PagedList.Config config = new PagedList.Config.Builder()
                 .setEnablePlaceholders(false)
                 .setPrefetchDistance(5)
                 .setPageSize(10)
                 .build();



         DatabasePagingOptions<Conversation> options = new DatabasePagingOptions.Builder<Conversation>()
                 .setLifecycleOwner(lifecycleOwner)
                 .setQuery(conversationDatabaseReference , config, Conversation.class)
                 .build();

         murmuroRepositoryImp.deleteAllConversations();

         FirebaseRecyclerPagingAdapter<Conversation, ConversationAdapter.MyViewHolder>  firebaseRecyclerPagingAdapter
                 = new FirebaseRecyclerPagingAdapter<Conversation, ConversationAdapter.MyViewHolder>(options) {
             @Override
             protected void onBindViewHolder(@NonNull ConversationAdapter.MyViewHolder myViewHolder, int i, @NonNull final Conversation conversation) {

                 Log.e(TAG, "onBindViewHolder: " + conversation.getMembers().get(0).getName() );
                 Log.e(TAG, "onBindViewHolder: " + conversation.getMembers().get(1).getName() );
                 Log.e(TAG, "onBindViewHolder: " + searchQuety );

                 if(!searchQuety.equals(""))
                 {

                     if(conversation.getMembers().get(0).getName().toLowerCase().contains(searchQuety.toLowerCase()) ||
                             conversation.getMembers().get(1).getName().toLowerCase().contains(searchQuety.toLowerCase()))
                     {
                         myViewHolder.bind(conversation);
                         conversationList.add(conversation);
                     }else
                     {
                         myViewHolder.itemView.setVisibility(GONE);
                     }

                 }else
                 {
                     myViewHolder.bind(conversation);
                     conversationList.add(conversation);
                 }


                 Log.e(TAG, "onBindViewHolder: " + conversation.getDisplayMessage() );
                 List<Person> people = conversation.getMembers();

                 int friendUSer = 0;
                 int currentUser = 1;

                 if(people!= null && people.size()>0)
                 {
                     if(people.get(0).getId().equals(firebaseAuth.getCurrentUser().getUid()))
                     {
                         friendUSer = 1;
                         currentUser = 0;
                     }
                 }


                 final TextView chat_name  =   myViewHolder.itemView.findViewById(R.id.chat_name);
                 TextView display_message  =   myViewHolder.itemView.findViewById(R.id.chat_display_message);
                 TextView unread_messages  =   myViewHolder.itemView.findViewById(R.id.unread_messages);
                 final CircleImageView chat_photo =  myViewHolder.itemView.findViewById(R.id.message_image);
                 TextView chat_time = myViewHolder.itemView.findViewById(R.id.chat_time);
                 TextView chat_date = myViewHolder.itemView.findViewById(R.id.chat_date);

                 chat_time.setText(conversation.getDisplayMessage().getDateTime().toString().substring(8,10) + " : " + conversation.getDisplayMessage().getDateTime().toString().substring(10,12));
                 chat_date.setText(conversation.getDisplayMessage().getDateTime().toString().substring(6,8)+ " - " + conversation.getDisplayMessage().getDateTime().toString().substring(4,6));

                 int unreadmessages_number = (int) Double.parseDouble(conversation.getUndreadMessages().get(conversation.getMembers().get(currentUser).getId()).toString());

                 if(unreadmessages_number > 0 )
                 {
                     unread_messages.setVisibility(View.VISIBLE);
                     unread_messages.setText(unreadmessages_number + "");
                     display_message.setTextColor(Color.BLACK);
                 }else{
                     unread_messages.setVisibility(GONE);
                 }

                 DatabaseReference databaseReference = firebaseDatabase.getReference().child("users").child(conversation.getMembers().get(friendUSer).getId());

                 databaseReference.addValueEventListener(new ValueEventListener() {
                     @Override
                     public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                         User user = dataSnapshot.getValue(User.class);

                         chat_name.setText(user.getName());

                         firebaseStorage.getReference().child("images/"+ user.getPhoto()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                             @Override
                             public void onSuccess(Uri uri) {
                                 requestManager.load(uri.toString()).into(chat_photo);
                             }
                         });

                     }

                     @Override
                     public void onCancelled(@NonNull DatabaseError databaseError) {
                         Log.e(TAG, "onCancelled: " +  "can not load photo or name " );
                     }
                 });

                myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         MainActivity.bottomNavigationView.setVisibility(GONE);
                         Navigation.findNavController(getActivity(), R.id.host_fragment)
                                 .navigate(ConversationsDirections.actionConversationsToChat(conversation.getId()));
                     }
                 });

             }

             @Override
             protected void onLoadingStateChanged(@NonNull LoadingState state) {
                 switch (state) {
                     case LOADING_INITIAL:
                     case LOADING_MORE:
                         // Do your loading animation

                         conversationAdapterDataResourceMutableLiveData.setValue(DataResource.loading(
                                 (FirebaseRecyclerPagingAdapter<Conversation, ConversationAdapter.MyViewHolder>)null));
                         break;

                     case LOADED:
                         // Stop Animation
                         if(conversations != null)
                         {
                             conversations.setValue(DataResource.success(conversationList));
                         }

                         if(conversationList != null)
                         {
                             conversationList.clear();
                         }
                         conversationAdapterDataResourceMutableLiveData.setValue(DataResource.error("LOADED" ,
                                 (FirebaseRecyclerPagingAdapter<Conversation, ConversationAdapter.MyViewHolder>)null));
                         break;

                     case FINISHED:
                         //Reached end of Data set
                         conversationAdapterDataResourceMutableLiveData.setValue(DataResource.error("FINISHED" ,
                                 (FirebaseRecyclerPagingAdapter<Conversation, ConversationAdapter.MyViewHolder>)null));
                         if(conversations != null)
                         {
                             conversations.setValue(DataResource.error("" , (List<Conversation>) conversationList));
                         }
                         break;


                     case ERROR:
                         retry();
                         break;
                 }
             }

             @NonNull
             @Override
             public ConversationAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                 LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
                 ViewDataBinding binding = DataBindingUtil.inflate(layoutInflater, R.layout.conversation_item, parent, false);
                 return new ConversationAdapter.MyViewHolder(binding);
             }

             @Override
             protected void onError(@NonNull DatabaseError databaseError) {
                 super.onError(databaseError);
                 databaseError.toException().printStackTrace();
                 conversationAdapterDataResourceMutableLiveData.setValue(DataResource.error(databaseError.getMessage() , (FirebaseRecyclerPagingAdapter<Conversation, ConversationAdapter.MyViewHolder>)null));
             }
         };

         conversationAdapterDataResourceMutableLiveData.setValue(DataResource.success(firebaseRecyclerPagingAdapter));
     }else
     {
         murmuroRepositoryImp.getConversations()
                 .subscribeOn(Schedulers.io())
                 .observeOn(AndroidSchedulers.mainThread())
                 .onErrorReturn(new Function<Throwable, List<Conversation>>() {
                     @Override
                     public List<Conversation> apply(Throwable throwable) throws Exception {
                         List<Conversation> list = new ArrayList<>();
                         Conversation conversation = new Conversation();
                         conversation.setId("-1");
                         list.add(conversation);
                         return list;
                     }
                 })
                 .map(new Function<List<Conversation>, Object>() {
                     @Override
                     public Object apply(List<Conversation> conversations) throws Exception {

                         if (conversations.size() > 0)
                         {
                             if(conversations.get(0).getId().equals("-1"))
                             {
                              //   conversationDataResourceMutableLiveData.setValue(DataResource.error("can not load conversation" , (List<Conversation>) null));
                                 return null;
                             }
                         }

                         //conversationDataResourceMutableLiveData.setValue(DataResource.success(conversations));

                         return conversations;
                     }
                 }).subscribe();
     }

        return conversationAdapterDataResourceMutableLiveData;
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
