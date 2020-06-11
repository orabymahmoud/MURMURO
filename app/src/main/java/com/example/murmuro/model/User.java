package com.example.murmuro.model;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.library.baseAdapters.BR;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
@Entity(tableName = "user")
public class User extends BaseObservable {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "username")
    private String username;

    @ColumnInfo(name = "password")
    private String password;

    @ColumnInfo(name = "photo")
    private String photo;

    @ColumnInfo(name = "email")
    private String email;

    @ColumnInfo(name = "city")
    private String city;

    @ColumnInfo(name = "mobile")
    private String mobile;

    @ColumnInfo(name = "bio")
    private String bio;

    @ColumnInfo(name = "status")
    private String status;

    @ColumnInfo(name = "conversations")
    private HashMap<String, Conversation>  conversations;

    public User() {
    }

    public User(String id, String name, String username, String password, String photo, String email, String city, String mobile, String bio, String status,HashMap<String, Conversation>conversations) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.password = password;
        this.photo = photo;
        this.email = email;
        this.city = city;
        this.mobile = mobile;
        this.bio = bio;
        this.status = status;
        this.conversations = conversations;
    }



    @Bindable
    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
        notifyPropertyChanged(BR.bio);
    }
    @Bindable
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        notifyPropertyChanged(BR.status);
    }
    @Bindable
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        notifyPropertyChanged(BR.id);
    }
    @Bindable
    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
        notifyPropertyChanged(BR.mobile);
    }
    @Bindable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        notifyPropertyChanged(BR.name);
    }
    @Bindable
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        notifyPropertyChanged(BR.username);
    }
    @Bindable
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
        notifyPropertyChanged(BR.city);
    }
    @Bindable
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        notifyPropertyChanged(BR.password);
    }
    @Bindable
    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
        notifyPropertyChanged(BR.photo);
    }
    @Bindable
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        notifyPropertyChanged(BR.email);
    }

    @Bindable
    public HashMap<String, Conversation> getConversations() {
        return conversations;
    }

    public void setConversations(HashMap<String, Conversation> conversations) {
        this.conversations = conversations;
        notifyPropertyChanged(BR.conversations);
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("name", name);
        result.put("email", email);
        result.put("username", username);
        result.put("password", password);
        result.put("city", city);
        result.put("mobile", mobile);
        result.put("status", status);
        result.put("bio", bio);
        result.put("photo", photo);
        result.put("conversations" , conversations);

        return result;
    }

}
