package com.bencww.learning.weiwen.models;

import java.util.List;

/**
 * Created by BenWwChen on 2017/1/18.
 */

// one section per user, contains user info and 3 latest posts of the user
public class ExploreSection {
    private User user;
    private boolean isFollowed;
    private List<Post> posts;

    public ExploreSection(User user, boolean isFollowed, List<Post> posts) {
        this.user = user;
        this.isFollowed = isFollowed;
        this.posts = posts;
    }

    public User getUser() {
        return user;
    }

    public boolean getIsFollowed() {
        return isFollowed;
    }

    public List<Post> getPosts() {
        return posts;
    }
}
