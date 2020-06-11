package com.example.murmuro.model;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.example.murmuro.BR;

public class Message extends BaseObservable {

    private String id;
    private String messageType;
    private String text;
    private String video;
    private String audio;
    private String photo;
    private Long dateTime;
    private Person sentBy;
    private String status;

    public Message() {
    }

    public Message(String id, String messageType, String text, String video, String audio, String photo, Long dateTime, Person sentBy, String status) {
        this.id = id;
        this.messageType = messageType;
        this.text = text;
        this.video = video;
        this.audio = audio;
        this.photo = photo;
        this.dateTime = dateTime;
        this.sentBy = sentBy;
        this.status = status;
    }

    @Bindable
    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
        notifyPropertyChanged(BR.messageType);
    }
    @Bindable
    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
        notifyPropertyChanged(BR.video);
    }
    @Bindable
    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
        notifyPropertyChanged(BR.audio);
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
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        notifyPropertyChanged(BR.id);
    }

    @Bindable
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        notifyPropertyChanged(BR.text);
    }


    @Bindable
    public Long getDateTime() {
        return dateTime;
    }

    public void setDateTime(Long dateTime) {
        this.dateTime = dateTime;
        notifyPropertyChanged(BR.dateTime);
    }

    @Bindable
    public Person getSentBy() {
        return sentBy;
    }

    public void setSentBy(Person sentBy) {
        this.sentBy = sentBy;
        notifyPropertyChanged(BR.sentBy);
    }

    @Bindable
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        notifyPropertyChanged(BR.status);
    }
}