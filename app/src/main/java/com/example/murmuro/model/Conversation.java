package com.example.murmuro.model;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.murmuro.BR;
import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity(tableName = "conversations")
public class Conversation extends BaseObservable {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "displayMessage")
    private Message displayMessage;

    @ColumnInfo(name = "lastDateTime")
    private Long lastDateTime;

    @ColumnInfo(name = "members")
    private List<Person> members;

    @ColumnInfo(name = "messages")
    private HashMap<String, Message> messages;

    @ColumnInfo(name = "type")
    private String type;

    @ColumnInfo(name = "undreadMessages")
    private HashMap<String, Object> undreadMessages;

    @ColumnInfo(name = "lastMessageId")
    private String lastMessageId;

    public Conversation() {
    }

    public Conversation(@NonNull String id, String name, Message displayMessage, Long lastDateTime, List<Person> members, HashMap<String, Message> messages, String type, HashMap<String, Object> undreadMessages, String lastMessageId) {
        this.id = id;
        this.name = name;
        this.displayMessage = displayMessage;
        this.lastDateTime = lastDateTime;
        this.members = members;
        this.messages = messages;
        this.type = type;
        this.undreadMessages = undreadMessages;
        this.lastMessageId = lastMessageId;
    }

    @Bindable
    public HashMap<String, Object> getUndreadMessages() {
        return undreadMessages;
    }

    public void setUndreadMessages(HashMap<String, Object> undreadMessages) {
        this.undreadMessages = undreadMessages;
        notifyPropertyChanged(BR.undreadMessages);
    }

    @Bindable
    public String getLastMessageId() {
        return lastMessageId;
    }

    public void setLastMessageId(String lastMessageId) {
        this.lastMessageId = lastMessageId;
        notifyPropertyChanged(BR.lastMessageId);
    }

    @Bindable
    public String getType() {
        return type;

    }

    public void setType(String type) {
        this.type = type;
        notifyPropertyChanged(BR.type);
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
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        notifyPropertyChanged(BR.id);
    }

    @Bindable
    public Message getDisplayMessage() {
        return displayMessage;
    }

    public void setDisplayMessage(Message displayMessage) {
        this.displayMessage = displayMessage;
        notifyPropertyChanged(BR.displayMessage);
    }

    @Bindable
    public List<Person> getMembers() {
        return members;
    }

    public void setMembers(List<Person> members) {
        this.members = members;
        notifyPropertyChanged(BR.members);
    }

    @Bindable
    public HashMap<String, Message> getMessages() {
        return messages;
    }

    public void setMessages(HashMap<String, Message> messages) {
        this.messages = messages;
        notifyPropertyChanged(BR.messages);
    }

    @Bindable
    public Long getLastDateTime() {
        return lastDateTime;
    }

    public void setLastDateTime(Long lastDateTime) {
        this.lastDateTime = lastDateTime;
        notifyPropertyChanged(BR.lastDateTime);
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("name", name);
        result.put("displayMessage", displayMessage);
        result.put("lastDateTime", lastDateTime);
        result.put("members", members);
        result.put("messages", messages);
        result.put("type" , type);
        result.put("undreadMessages" , undreadMessages);
        result.put("lastMessageId" , lastMessageId);
        return result;
    }
    @Exclude
    public Map<String, Object> toMapWithoutMessages() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("name", name);
        result.put("displayMessage", displayMessage);
        result.put("lastDateTime", lastDateTime);
        result.put("members", members);
        result.put("type" , type);
        result.put("undreadMessages" , undreadMessages);
        return result;
    }


}
