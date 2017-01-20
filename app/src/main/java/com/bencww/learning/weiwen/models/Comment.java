package com.bencww.learning.weiwen.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by BenWwChen on 2017/1/17.
 */

public class Comment {

    @SerializedName("comment_id")
    private Integer commentId;
    @SerializedName("user_id")
    private Integer userId;
    @SerializedName("post_id")
    private Integer postId;
    @SerializedName("comment_content")
    private String content;
    @SerializedName("create_time")
    private String createTime;
    private String username;

    public Comment(Integer commentId, Integer userId, Integer postId, String content, String createTime, String username) {
        this.commentId = commentId;
        this.userId = userId;
        this.postId = postId;
        this.content = content;
        this.createTime = createTime;
        this.username = username;
    }

    public Integer getCommentId() {
        return commentId;
    }

    public Integer getUserId() {
        return userId;
    }

    public Integer getPostId() {
        return postId;
    }

    public String getContent() {
        return content;
    }

    public String getCreateTime() {
        return createTime;
    }

    public String getUsername() {
        return username;
    }

    public static List<Comment> commentSampleList = initCommentSampleData();
    private static List<Comment> initCommentSampleData() {
        Comment[] commentsArray = {
                new Comment(5, 3, 12, "haha", "1 个月前", "ben"),
                new Comment(5, 3, 12, "haha2", "1 个月前", "ben2"),
                new Comment(5, 3, 12, "haha3", "1 个月前", "ben3"),
        };

        List<Comment> commentsList = Arrays.asList(commentsArray);

        return commentsList;
    }


}
