package com.bencww.learning.weiwen.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.bencww.learning.weiwen.HomepageActivity;
import com.bencww.learning.weiwen.R;
import com.bencww.learning.weiwen.models.Post;
import com.bencww.learning.weiwen.models.Comment;
import com.bencww.learning.weiwen.utils.VolleyUtil;
import com.bencww.learning.weiwen.utils.WeiwenApiClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by BenWwChen on 2017/1/14.
 */

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    private PostsAdapterCallback mPostsAdapterCallback;
    private ArrayList<Post> mPosts;
    private String mUsername;
    ImageLoader mImageLoader;
    Context context;
    private int picMaxWidth;
    private int picMaxHeight;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView avatarImageView;
        public TextView userNameTextView;
        public TextView timeTextView;
        public ImageView picImageView;
        public LinearLayout commentsLayout;
        public Button deleteButton;
        public Button likeButton;
        public Button commentButton;

        public ViewHolder(View itemView) {
            super(itemView);
            avatarImageView = (ImageView) itemView.findViewById(R.id.avatar_image_view);
            userNameTextView = (TextView) itemView.findViewById(R.id.username_text_view);
            timeTextView = (TextView) itemView.findViewById(R.id.time_text_view);
            picImageView = (ImageView) itemView.findViewById(R.id.pic_image_view);
            commentsLayout = (LinearLayout) itemView.findViewById(R.id.comments_layout);
            deleteButton = (Button) itemView.findViewById(R.id.delete_button);
            likeButton = (Button) itemView.findViewById(R.id.like_button);
            commentButton = (Button) itemView.findViewById(R.id.comment_button);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public PostsAdapter(ArrayList<Post> posts, Context context, PostsAdapterCallback callback) {
        this.context = context;
        this.mPosts = posts;
        this.mPostsAdapterCallback = callback;
        // Get the ImageLoader through your singleton class.
        mImageLoader = VolleyUtil.getInstance(context).getImageLoader();
        // Get pic max width/height (display width/height)
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        picMaxWidth = dm.widthPixels;
        picMaxHeight = dm.heightPixels;
        mUsername = WeiwenApiClient.getInstance(context).getUser().getUsername();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public PostsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.post_list_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        final Post curPost = mPosts.get(position);

        // username, create time
        holder.userNameTextView.setText(curPost.getUsername());
        holder.timeTextView.setText(curPost.getCreateTime());

        // avatar, pic
        ImageView avatarImageView = holder.avatarImageView;
        ImageView picImageView = holder.picImageView;
        String avatarUrl = WeiwenApiClient.getAvatarUrl(curPost.getAvatar());
        String picUrl = WeiwenApiClient.getPicUrl(curPost.getUrl());

        mImageLoader.get(avatarUrl, ImageLoader.getImageListener(avatarImageView,
                R.color.cardview_light_background, R.color.cardview_light_background), 100, 100);

        mImageLoader.get(picUrl, ImageLoader.getImageListener(picImageView,
                R.color.cardview_light_background, R.color.cardview_light_background), picMaxWidth,
                picMaxHeight);

        // click username or avatar jump to homepage
        View.OnClickListener nameAvatarOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, HomepageActivity.class);
                intent.putExtra("username", curPost.getUsername());
                context.startActivity(intent);
            }
        };
        holder.userNameTextView.setOnClickListener(nameAvatarOnClickListener);
        holder.avatarImageView.setOnClickListener(nameAvatarOnClickListener);

        // like count, description
        holder.commentsLayout.removeAllViewsInLayout();
        if (curPost.getLikeCount() != 0) {
            View likeCountView = getCommentView(holder, String.valueOf(curPost.getLikeCount())
                    , context.getString(R.string.like_count_text));
            likeCountView.findViewById(R.id.comment_username_text_view).setOnClickListener(null);
            holder.commentsLayout.addView(likeCountView);
        }
        View descriptionView = getCommentView(holder, curPost.getUsername(), curPost.getDescription());
        holder.commentsLayout.addView(descriptionView);

        // comments
        List<Comment> comments = curPost.getComments();
        for (int i = 0; i < comments.size(); i++) {
            final Comment curComment = comments.get(i);
            View commentView = getCommentView(holder,
                    curComment.getUsername(), curComment.getContent());
            holder.commentsLayout.addView(commentView);
        }

        // delete button
        if (curPost.getUsername().equals(mUsername)) {
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mPostsAdapterCallback.onDeleteButtonClick(curPost.getPostId());
                }
            });
        } else {
            holder.deleteButton.setVisibility(View.INVISIBLE);
            holder.deleteButton.setOnClickListener(null);
        }

        // like button and comment button
        final boolean isLiked = curPost.isLiked();
        if (isLiked) {
            holder.likeButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like, 0, 0, 0);
        } else {
            holder.likeButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_notlike, 0, 0, 0);
        }
        // let the fragment handle the clicks
        holder.likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // change the icon first (if failed, the fragment will refresh the data back)
                if (isLiked) {
                    holder.likeButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_notlike, 0, 0, 0);
                } else {
                    holder.likeButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like, 0, 0, 0);
                }
                mPostsAdapterCallback.onLikeButtonClick(curPost.getPostId(), !isLiked);
            }
        });

        holder.commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPostsAdapterCallback.onCommentButtonClick(curPost.getPostId());
            }
        });
    }

    public interface PostsAdapterCallback {
        void onLikeButtonClick(int postId, boolean like);
        void onCommentButtonClick(int postId);
        void onDeleteButtonClick(int postId);
    }

    private View getCommentView(ViewHolder holder, final String name, String content) {
        View commentView = LayoutInflater.from(holder.commentsLayout.getContext())
                .inflate(R.layout.comment_list_item, holder.commentsLayout, false);
        TextView usernameTextView = (TextView) commentView.findViewById(R.id.comment_username_text_view);
        usernameTextView.setText(name);
        usernameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, HomepageActivity.class);
                intent.putExtra("username", name);
                context.startActivity(intent);
            }
        });
        ((TextView) commentView.findViewById(R.id.comment_content_text_view)).setText(content);
        return commentView;
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mPosts.size();
    }
}
