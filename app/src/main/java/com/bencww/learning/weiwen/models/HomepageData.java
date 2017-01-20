package com.bencww.learning.weiwen.models;

import java.util.List;

/**
 * Created by BenWwChen on 2017/1/20.
 */

public class HomepageData {
    boolean isFollowed;
    User user;
    List<Post> posts;

    public HomepageData(boolean isFollowed, User user, List<Post> posts) {
        this.isFollowed = isFollowed;
        this.user = user;
        this.posts = posts;
    }

    public boolean isFollowed() {
        return isFollowed;
    }

    public User getUser() {
        return user;
    }

    public List<Post> getPosts() {
        return posts;
    }

    public void setFollowed(boolean followed) {
        isFollowed = followed;
    }
}
