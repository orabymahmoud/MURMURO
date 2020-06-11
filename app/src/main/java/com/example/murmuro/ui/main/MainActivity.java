package com.example.murmuro.ui.main;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.navigation.Navigation;
import com.example.murmuro.BaseActivity;
import com.example.murmuro.R;
import com.example.murmuro.databinding.ActivityMainBinding;
import com.example.murmuro.ui.main.calls.CallsDirections;
import com.example.murmuro.ui.main.conversations.ConversationsDirections;
import com.example.murmuro.ui.main.groups.GroupsDirections;
import com.example.murmuro.ui.main.people.PeopleDirections;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    public static int current = 0;
    public static BottomNavigationView bottomNavigationView;
    public static FloatingActionButton floatingActionButton_LiveTranslation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this , R.layout.activity_main);

        bottomNavigationView = binding.bottomNavigation;
        floatingActionButton_LiveTranslation = binding.liveTranslationFAB;

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.conversations_item:
                        if(Navigation.findNavController(MainActivity.this, R.id.host_fragment).getCurrentDestination() != null)
                        {
                            navigate(0);

                        }else{

                        }
                        break;
                    case R.id.people_item:
                        if(Navigation.findNavController(MainActivity.this, R.id.host_fragment).getCurrentDestination() != null)
                        {
                            navigate(1);

                        }else{

                        }
                        break;
                    case R.id.groups_item:
                        if(Navigation.findNavController(MainActivity.this, R.id.host_fragment).getCurrentDestination() != null)
                        {
                            navigate(2);

                        }else{

                        }
                        break;
                    case R.id.calls_item:
                        if(Navigation.findNavController(MainActivity.this, R.id.host_fragment).getCurrentDestination() != null)
                        {
                            navigate(3);

                        }else
                        {

                        }
                        break;
                }
                return true;
            }
        });


        MainActivity.floatingActionButton_LiveTranslation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( Navigation.findNavController(MainActivity.this, R.id.host_fragment).getCurrentDestination().getId() == R.id.conversations)
                {
                    Navigation.findNavController(MainActivity.this, R.id.host_fragment).navigate(ConversationsDirections.actionConversationsToLiveTranslation());

                }else if(Navigation.findNavController(MainActivity.this, R.id.host_fragment).getCurrentDestination().getId() == R.id.poeple)
                {
                    Navigation.findNavController(MainActivity.this, R.id.host_fragment).navigate(PeopleDirections.actionPoepleToLiveTranslation());

                }else if(Navigation.findNavController(MainActivity.this, R.id.host_fragment).getCurrentDestination().getId() == R.id.groups)
                {
                    Navigation.findNavController(MainActivity.this, R.id.host_fragment).navigate(GroupsDirections.actionGroupsToLiveTranslation());

                }else if(Navigation.findNavController(MainActivity.this, R.id.host_fragment).getCurrentDestination().getId() == R.id.calls)
                {
                    Navigation.findNavController(MainActivity.this, R.id.host_fragment).navigate(CallsDirections.actionCallsToLiveTranslation());

                }

                MainActivity.bottomNavigationView.setVisibility(View.GONE);
                MainActivity.floatingActionButton_LiveTranslation.setVisibility(View.GONE);
            }
        });



    }

    public void navigate(int i) {

      if(Navigation.findNavController(this, R.id.host_fragment).getCurrentDestination() != null)
      {
          if( Navigation.findNavController(this, R.id.host_fragment).getCurrentDestination().getId() == R.id.conversations)
          {
              if(i == 0)
              {

              }else if(i == 1)
              {
                  Navigation.findNavController(this, R.id.host_fragment).navigate(ConversationsDirections.actionConversationsToPoeple());

              }else if(i == 2)
              {
                  Navigation.findNavController(this, R.id.host_fragment).navigate(ConversationsDirections.actionConversationsToGroups());

              }else if(i == 3)
              {
                  Navigation.findNavController(this, R.id.host_fragment).navigate(ConversationsDirections.actionConversationsToCalls());
              }

          }else if(Navigation.findNavController(this, R.id.host_fragment).getCurrentDestination().getId() == R.id.poeple)
          {
              if(i == 0)
              {
                  Navigation.findNavController(this, R.id.host_fragment).navigate(PeopleDirections.actionPoepleToConversations());
              }else if(i == 1)
              {

              }else if(i == 2)
              {
                  Navigation.findNavController(this, R.id.host_fragment).navigate(PeopleDirections.actionPoepleToGroups());
              }else if(i == 3)
              {
                  Navigation.findNavController(this, R.id.host_fragment).navigate(PeopleDirections.actionPoepleToCalls());
              }
          }else if(Navigation.findNavController(this, R.id.host_fragment).getCurrentDestination().getId() == R.id.groups)
          {
              if(i == 0)
              {
                  Navigation.findNavController(this, R.id.host_fragment).navigate(GroupsDirections.actionGroupsToConversations());
              }else if(i == 1)
              {
                  Navigation.findNavController(this, R.id.host_fragment).navigate(GroupsDirections.actionGroupsToPoeple());
              }else if(i == 2)
              {

              }else if(i == 3)
              {
                  Navigation.findNavController(this, R.id.host_fragment).navigate(GroupsDirections.actionGroupsToCalls());
              }
          }else if(Navigation.findNavController(this, R.id.host_fragment).getCurrentDestination().getId() == R.id.calls)
          {
              if(i == 0)
              {
                  Navigation.findNavController(this, R.id.host_fragment).navigate(CallsDirections.actionCallsToConversations());

              }else if(i == 1)
              {
                  Navigation.findNavController(this, R.id.host_fragment).navigate(CallsDirections.actionCallsToPoeple());

              }else if(i == 2)
              {
                  Navigation.findNavController(this, R.id.host_fragment).navigate(CallsDirections.actionCallsToGroups());

              }else if(i == 3)
              {

              }
          }
      }else
      {

      }

    }



    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBundle("nav_state", Navigation.findNavController(this, R.id.host_fragment).saveState());

    }


    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Navigation.findNavController(this, R.id.host_fragment).restoreState(savedInstanceState.getBundle("nav_state"));
        Navigation.findNavController(this, R.id.host_fragment).setGraph(R.navigation.main_nav);


    }


}
