package com.bencww.learning.weiwen;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.bencww.learning.weiwen.adapters.PostsAdapter;
import com.bencww.learning.weiwen.models.Comment;
import com.bencww.learning.weiwen.models.Post;
import com.bencww.learning.weiwen.utils.VolleyUtil;
import com.bencww.learning.weiwen.utils.WeiwenApiClient;

import java.util.Collections;
import java.util.List;

public class DetailActivity extends AppCompatActivity {

    private int mPostId;
    private Post mPost;

    private static final int ERROR_CODE = -1;
    private static final int COMMENT_CODE = 1;
    private static final int UPDATE_COMMENT = 1;
    private static final int NO_UPDATE_COMMENT = 0;

    // views
    private ImageView mAvatarImageView;
    private TextView mUserNameTextView;
    private TextView mTimeTextView;
    private ImageView mPicImageView;
    private LinearLayout mCommentsLayout;
    private Button mLikeButton;
    private Button mCommentButton;

    // utils
    ImageLoader mImageLoader;
    private int picMaxWidth;
    private int picMaxHeight;
    private Handler mDataHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        setTitle(R.string.title_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        View postView = findViewById(R.id.post_list_item);

        mPostId = getIntent().getIntExtra("postId", ERROR_CODE);
        if (mPostId == ERROR_CODE) {
            // Maybe Toast
            finish();
        }

        // get views
        mAvatarImageView = (ImageView) postView.findViewById(R.id.avatar_image_view);
        mUserNameTextView = (TextView) postView.findViewById(R.id.username_text_view);
        mTimeTextView = (TextView) postView.findViewById(R.id.time_text_view);
        mPicImageView = (ImageView) postView.findViewById(R.id.pic_image_view);
        mCommentsLayout = (LinearLayout) postView.findViewById(R.id.comments_layout);
        mLikeButton = (Button) postView.findViewById(R.id.like_button);
        mCommentButton = (Button) postView.findViewById(R.id.comment_button);

        // utils
        mImageLoader = VolleyUtil.getInstance(this).getImageLoader();
        // Get pic max width/height (display width/height)
        DisplayMetrics dm = getResources().getDisplayMetrics();
        picMaxWidth = dm.widthPixels;
        picMaxHeight = dm.heightPixels;

        // handle post data
        mDataHandler = new Handler() {

            @Override
            public void handleMessage(android.os.Message msg) {

                if(msg.what == 1){
                    mPost = (Post) msg.obj;
                } else {
                    Log.d("error", (String) msg.obj);
                }

                updateData();
            };
        };

        loadData();

    }

    private void loadData() {
        WeiwenApiClient.getInstance(this).getPost(mPostId, mDataHandler);
    }

    private void updateData() {
        // username, create time
        mUserNameTextView.setText(mPost.getUsername());
        mTimeTextView.setText(mPost.getCreateTime());

        // avatar, pic
        ImageView avatarImageView = mAvatarImageView;
        ImageView picImageView = mPicImageView;
        String avatarUrl = WeiwenApiClient.getAvatarUrl(mPost.getAvatar());
        String picUrl = WeiwenApiClient.getPicUrl(mPost.getUrl());

        mImageLoader.get(avatarUrl, ImageLoader.getImageListener(avatarImageView,
                R.color.cardview_light_background, R.color.cardview_light_background), 100, 100);

        mImageLoader.get(picUrl, ImageLoader.getImageListener(picImageView,
                R.color.cardview_light_background, R.color.cardview_light_background), picMaxWidth,
                picMaxHeight);

        // click username or avatar jump to homepage
        View.OnClickListener nameAvatarOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DetailActivity.this, HomepageActivity.class);
                intent.putExtra("username", mPost.getUsername());
                startActivity(intent);
            }
        };
        mUserNameTextView.setOnClickListener(nameAvatarOnClickListener);
        mAvatarImageView.setOnClickListener(nameAvatarOnClickListener);

        // like count, description
        mCommentsLayout.removeAllViewsInLayout();
        if (mPost.getLikeCount() != 0) {
            View likeCountView = getCommentView(String.valueOf(mPost.getLikeCount())
                    , getString(R.string.like_count_text));
            likeCountView.findViewById(R.id.comment_username_text_view).setOnClickListener(null);
            mCommentsLayout.addView(likeCountView);
        }
        View descriptionView = getCommentView(mPost.getUsername(), mPost.getDescription());
        mCommentsLayout.addView(descriptionView);

        // comments
        List<Comment> comments = mPost.getComments();
        for (int i = 0; i < comments.size(); i++) {
            final Comment curComment = comments.get(i);
            View commentView = getCommentView(curComment.getUsername(), curComment.getContent());
            mCommentsLayout.addView(commentView);
        }

        // like button and comment button
        final boolean isLiked = mPost.isLiked();
        if (isLiked) {
            mLikeButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like, 0, 0, 0);
        } else {
            mLikeButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_notlike, 0, 0, 0);
        }
        // let the fragment handle the clicks
        mLikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // change the icon first (if failed, the fragment will refresh the data back)
                if (isLiked) {
                    mLikeButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_notlike, 0, 0, 0);
                } else {
                    mLikeButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like, 0, 0, 0);
                }
                onLikeButtonClick(mPost.getPostId(), !isLiked);
            }
        });

        mCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCommentButtonClick(mPost.getPostId());
            }
        });
    }

    private void onLikeButtonClick(int postId, boolean like) {
        Handler likePostHandler = new Handler() {
            @Override
            public void handleMessage(android.os.Message msg) {
                boolean isSuccess;

                if(msg.what == 1){

                    isSuccess = (boolean) msg.obj;
                } else {
                    Log.d("error", (String) msg.obj);
                    Toast.makeText(getApplicationContext(), "错误:" + msg.obj, Toast.LENGTH_SHORT).show();
                    isSuccess = false;
                }

                loadData();
            }
        };
        WeiwenApiClient.getInstance(this).setLikeForAPost(postId, like, likePostHandler);
    }

    private void onCommentButtonClick(int postId) {
        Intent intent = new Intent(DetailActivity.this, CommentActivity.class);
        intent.putExtra("postId", postId);
        startActivityForResult(intent, COMMENT_CODE);
    }

    private View getCommentView(final String name, String content) {
        View commentView = LayoutInflater.from(mCommentsLayout.getContext())
                .inflate(R.layout.comment_list_item, mCommentsLayout, false);
        TextView usernameTextView = (TextView) commentView.findViewById(R.id.comment_username_text_view);
        usernameTextView.setText(name);
        usernameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DetailActivity.this, HomepageActivity.class);
                intent.putExtra("username", name);
                startActivity(intent);
            }
        });
        ((TextView) commentView.findViewById(R.id.comment_content_text_view)).setText(content);
        return commentView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case COMMENT_CODE: {
                if (resultCode == UPDATE_COMMENT) {
                    loadData();
                }
            }
        }
    }
}
