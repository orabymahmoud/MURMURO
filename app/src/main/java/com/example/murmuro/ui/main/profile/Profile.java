package com.example.murmuro.ui.main.profile;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.RequestManager;
import com.example.murmuro.R;
import com.example.murmuro.databinding.ProfileFragmentBinding;
import com.example.murmuro.model.DataResource;
import com.example.murmuro.model.User;
import com.example.murmuro.ui.auth.AuthActivity;
import com.example.murmuro.ui.main.MainActivity;
import com.example.murmuro.viewModel.ViewModelProviderFactory;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

import static android.app.Activity.RESULT_OK;
import static android.view.View.VISIBLE;
import static androidx.constraintlayout.widget.Constraints.TAG;

public class Profile extends DaggerFragment {

    private static final int PICK_IMAGE_REQUEST = 40;
    private ProfileViewModel mViewModel;
    private ProfileFragmentBinding binding;

    @Inject
    FirebaseStorage firebaseStorage;

    @Inject
    FirebaseDatabase firebaseDatabase;

    @Inject
    FirebaseAuth firebaseAuth;

    @Inject
    RequestManager requestManager;

    @Inject
    ViewModelProviderFactory providerFactory;

    private User user = null;

    public static Profile newInstance() {
        return new Profile();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.profile_fragment, container, false);

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this, providerFactory).get(ProfileViewModel.class);
        if(savedInstanceState != null)
        {
            Navigation.findNavController(getActivity(), R.id.host_fragment).restoreState(savedInstanceState.getBundle("nav_state"));
            Log.e(TAG, "onRestoreInstanceState: " +  savedInstanceState.getBundle("nav_state").describeContents());
        }


        mViewModel.getCurrentUserDataResourceMutableLiveData().observe(getViewLifecycleOwner(), new Observer<DataResource<User>>() {
            @Override
            public void onChanged(DataResource<User> userDataResource) {
                if(userDataResource != null)
                {
                    switch (userDataResource.status)
                    {
                        case ERROR:
                        {
                            binding.progressBar.setVisibility(View.GONE);
                            Toast.makeText(getActivity() , userDataResource.message , Toast.LENGTH_SHORT).show();
                            break;
                        }

                        case LOADING:
                        {
                            binding.progressBar.setVisibility(View.VISIBLE);
                            break;
                        }

                        case SUCCESS:
                        {
                            binding.progressBar.setVisibility(View.GONE);
                            binding.setPerson(userDataResource.data);
                            user = userDataResource.data;
                            firebaseStorage.getReference().child("images/"+ userDataResource.data.getPhoto()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    requestManager.load(uri.toString()).into(binding.profileImage);
                                }
                            });

                            break;
                        }
                    }
                }
            }
        });




        binding.emailTitlebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(binding.editEmail.getVisibility() == VISIBLE)
                {
                    binding.editEmail.setVisibility(View.GONE);

                }else
                {
                    binding.editEmail.setVisibility(VISIBLE);
                }
            }
        });

        binding.nameTitlebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(binding.editName.getVisibility() == VISIBLE)
                {
                    binding.editName.setVisibility(View.GONE);

                }else
                {
                    binding.editName.setVisibility(VISIBLE);
                }
            }
        });

        binding.bioTitlebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(binding.editBio.getVisibility() == VISIBLE)
                {
                    binding.editBio.setVisibility(View.GONE);

                }else
                {
                    binding.editBio.setVisibility(VISIBLE);
                }
            }
        });

        binding.passwordTitlebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(binding.editPassword.getVisibility() == VISIBLE)
                {
                    binding.editPassword.setVisibility(View.GONE);

                }else
                {
                    binding.editPassword.setVisibility(VISIBLE);
                }
            }
        });

        binding.saveEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";

                if(binding.emailEditText.getText().toString().trim().matches(regex))
                {
                    DatabaseReference myRef = firebaseDatabase.
                            getReference("users").
                            child(firebaseAuth.getCurrentUser().getUid()).
                            child("email");

                    binding.progressBar.setVisibility(VISIBLE);

                    myRef.setValue(binding.emailEditText.getText().toString().trim())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mViewModel.updateCurrentUser();
                            binding.eMail.setText(binding.emailEditText.getText().toString().trim());
                            binding.progressBar.setVisibility(View.GONE);
                            binding.editEmail.setVisibility(View.GONE);

                        }
                    });
                }else
                {
                    binding.emailEditText.setError("Inavilable mail");

                }
            }
        });

        binding.saveName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String regx = "^[A-Za-z\\s]+$";

                if (binding.nameEditText.getText().toString().trim().matches(regx))
                {
                    DatabaseReference myRef = firebaseDatabase.
                            getReference("users").
                            child(firebaseAuth.getCurrentUser().getUid()).
                            child("name");
                    binding.progressBar.setVisibility(VISIBLE);
                    myRef.setValue((binding.nameEditText.getText().toString().trim()))
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    binding.progressBar.setVisibility(View.GONE);
                                    binding.profilename.setText(binding.nameEditText.getText().toString().trim());
                                    mViewModel.updateCurrentUser();
                                    binding.editName.setVisibility(View.GONE);
                                }
                            });

                }else
                {
                    binding.nameEditText.setError("Invalid name");
                }
            }
        });

        binding.saveBio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatabaseReference myRef = firebaseDatabase.
                        getReference("users").
                        child(firebaseAuth.getCurrentUser().getUid()).
                        child("bio");

                if(binding.bioEditText.getText().toString().trim().isEmpty())
                {
                    binding.bioEditText.setError("Inavlid Empty Bio");
                }else
                {
                    binding.progressBar.setVisibility(VISIBLE);
                    myRef.setValue(binding.bioEditText.getText().toString().trim())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    binding.progressBar.setVisibility(View.GONE);
                                    binding.editBio.setVisibility(View.GONE);
                                    mViewModel.updateCurrentUser();
                                    binding.profileBio.setText(binding.bioEditText.getText().toString().trim());
                                }
                            });
                }
            }
        });

        binding.savePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(binding.currentPasswordEditText.getText().toString().trim().equals(user.getPassword()))
                {
                    String pattern = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}";
                    boolean isPasswordValid = binding.newPasswordEditText.getText().toString().trim().matches(pattern);
                    if(isPasswordValid)
                    {
                        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        AuthCredential credential = EmailAuthProvider.getCredential(user.getUsername(),user.getPassword());


                        firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    firebaseUser.updatePassword(binding.newPasswordEditText.getText().toString().trim())
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(!task.isSuccessful()){
                                                DatabaseReference myRef = firebaseDatabase.
                                                        getReference("users").
                                                        child(firebaseAuth.getCurrentUser().getUid()).
                                                        child("password");
                                                binding.progressBar.setVisibility(VISIBLE);
                                                myRef.setValue(binding.newPasswordEditText.getText().toString().trim())
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                binding.progressBar.setVisibility(View.GONE);
                                                                binding.editPassword.setVisibility(View.GONE);
                                                                mViewModel.updateCurrentUser();
                                                            }
                                                        });
                                            }else {
                                                Toast.makeText(getContext(), "Can not change password now", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            }
                        });

                    }else
                    {
                        binding.newPasswordEditText.setError("Invalid Passwwword");
                    }
                }else
                {
                    binding.currentPasswordEditText.setError("Wronge password");
                }
            }
        });

        binding.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        binding.logoutFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(getContext()).setTitle("Log Out")
                        .setMessage("Are you sure?")
                        .setPositiveButton("YES",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                        firebaseAuth.signOut();
                                        Log.e(TAG, "onClick: " + firebaseAuth.getCurrentUser() );
                                        Intent intent = new Intent(getContext(), AuthActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        getActivity().finish();
                                    }
                                })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing
                                dialog.dismiss();
                            }
                        })
                        .create()
                        .show();

            }
        });
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null ) {

            final String filename = getFileName(data.getData());
            binding.progressBar.setVisibility(VISIBLE);
          StorageReference ref = firebaseStorage.getReference().child("images/"+ user.getUsername()+filename);
            ref.putFile(Uri.parse(data.getData().toString()))
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            DatabaseReference myRef = firebaseDatabase.
                                    getReference("users").
                                    child(firebaseAuth.getCurrentUser().getUid()).
                                    child("photo");

                            myRef.setValue(user.getUsername()+filename)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            binding.progressBar.setVisibility(View.GONE);
                                            mViewModel.updateCurrentUser();
                                            firebaseStorage.getReference().child("images/"+ user.getUsername()+filename).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    requestManager.load(uri.toString()).into(binding.profileImage);
                                                }
                                            });
                                        }
                                    });
                        }
                    });
        }


    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle("nav_state", Navigation.findNavController(getActivity(), R.id.host_fragment).saveState());
        Log.e(TAG, "onSaveInstanceState: " +  Navigation.findNavController(getActivity(), R.id.host_fragment).saveState());
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
            Log.e(TAG, "getFileName: " + cursor.toString() );
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

}
