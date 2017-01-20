package com.bencww.learning.weiwen.models;

import com.bencww.learning.weiwen.*;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by BenWwChen on 2017/1/14.
 */

public class Post {

    private String username;
    private String avatar;
    @SerializedName("post_id")
    private Integer postId;
    private String description;
    private String url;
    @SerializedName("user_id")
    private Integer userId;
    @SerializedName("create_time")
    private String createTime;
    private List<Comment> comments;
    private Integer likeCount;
    private boolean isLiked;

    public Post(String username, String avatar, Integer postId, String description, String url,
                Integer userId, String createTime, List<Comment> comments, Integer likeCount,
                boolean isLiked) {
        this.username = username;
        this.avatar = avatar;
        this.postId = postId;
        this.description = description;
        this.url = url;
        this.userId = userId;
        this.createTime = createTime;
        this.comments = comments;
        this.likeCount = likeCount;
        this.isLiked = isLiked;
    }

    public String getUsername() {
        return username;
    }

    public String getAvatar() {
        return avatar;
    }

    public Integer getPostId() {
        return postId;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getCreateTime() {
        return createTime;
    }

    public List<Comment> getComments() {
        if (comments == null) comments = Collections.emptyList();
        return comments;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public static List<Post> postSampleList = initPostSampleData();
    private static List<Post> initPostSampleData() {
        Post[] postsArray = {
                new Post("Ben", "/img/avatar.jpg", 12, "macOS.",
                        "upload_50dba0c7056d62376e2dd60bcd798656.jpg", 3, "1 个月前",
                        Comment.commentSampleList, 3, true),
                new Post("Ben2", "/img/avatar.jpg", 12, "macOS.2",
                        "upload_50dba0c7056d62376e2dd60bcd798656.jpg", 3, "1 个月前",
                        Comment.commentSampleList, 3, false),
                new Post("Ben3", "/img/avatar.jpg", 12, "macOS.3",
                        "upload_50dba0c7056d62376e2dd60bcd798656.jpg", 3, "1 个月前",
                        Comment.commentSampleList, 3, false)
        };

        List<Post> postsList = Arrays.asList(postsArray);

        return postsList;
    }
}
