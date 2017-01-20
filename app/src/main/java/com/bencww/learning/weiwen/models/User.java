package com.bencww.learning.weiwen.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by BenWwChen on 2017/1/17.
 */

public class User {

    @SerializedName("user_id")
    private Integer userId;
    private String username;
    private String email;
    private String bio;
    private String avatar;

    public User(Integer userId, String username, String email, String bio, String avatar) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.bio = bio;
        this.avatar = avatar;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getBio() {
        return bio;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
