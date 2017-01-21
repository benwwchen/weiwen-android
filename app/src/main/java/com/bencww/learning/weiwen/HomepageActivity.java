package com.bencww.learning.weiwen;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.bencww.learning.weiwen.adapters.PostsAdapter;
import com.bencww.learning.weiwen.models.HomepageData;
import com.bencww.learning.weiwen.models.Post;
import com.bencww.learning.weiwen.utils.VolleyUtil;
import com.bencww.learning.weiwen.utils.WeiwenApiClient;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomepageActivity extends AppCompatActivity {

    private HomepageData homepageData;
    private String mUsername;
    private boolean isSelf;
    private ImageLoader mImageLoader;
    private int picWidthOrHeight; // width/height of every pic

    private FloatingActionButton mFollowButton;

    private Handler mSetFollowHandler;
    private Handler mDataHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mImageLoader = VolleyUtil.getInstance(this).getImageLoader();

        setContentView(R.layout.activity_homepage);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // calculate the width/height of the pics on screen
        DisplayMetrics dm = getResources().getDisplayMetrics();
        picWidthOrHeight = (dm.widthPixels - dp2px(this, 64)) / 3;

        // get user data
        mUsername = this.getIntent().getStringExtra("username");

        mDataHandler = new Handler() {

            @Override
            public void handleMessage(android.os.Message msg) {

                if(msg.what == 1){
                    homepageData = (HomepageData) msg.obj;
                } else {
                    Log.d("error", (String) msg.obj);
                }

                // load data into views
                if (homepageData != null) {
                    updateData();
                }
            }
        };

        mFollowButton = (FloatingActionButton) findViewById(R.id.follow_button);

        mSetFollowHandler = new Handler() {
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
                if (isSuccess) {
                    String message = "已关注";
                    if (homepageData.isFollowed()) message = "已取消关注";
                    Snackbar.make(mFollowButton, message, Snackbar.LENGTH_LONG).show();
                    loadData();
                }
            }
        };

        isSelf = mUsername.equals(WeiwenApiClient.getInstance(this).getUser().getUsername());

        if (isSelf) {
            mFollowButton.setImageDrawable(
                    ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_mode_edit));
            mFollowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // edit avatar;
                }
            });
        } else {
            mFollowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    WeiwenApiClient.getInstance(getApplicationContext())
                            .setFollowByUsername(homepageData.getUser().getUsername(),
                            !homepageData.isFollowed(), mSetFollowHandler);
                }
            });
        }

        loadData();
    }

    private void loadData() {
        WeiwenApiClient.getInstance(this).getHomepageData(mUsername, mDataHandler);
    }

    private void updateData() {
        // load username, avatar
        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        collapsingToolbar.setTitle(homepageData.getUser().getUsername());

        ImageView avatarImageView = (ImageView) findViewById(R.id.avatar_image_view);
        String avatarUrl = WeiwenApiClient.getAvatarUrl(homepageData.getUser().getAvatar());
        mImageLoader.get(avatarUrl, ImageLoader.getImageListener(avatarImageView,
                R.color.cardview_light_background, R.color.cardview_light_background));

        // load bio
        TextView bioTextView = (TextView) findViewById(R.id.bio_text_view);
        bioTextView.setText(homepageData.getUser().getBio());

        // TODO: load counts

        // load posts
        LinearLayout postsLayout = (LinearLayout) findViewById(R.id.posts_layout);
        View postsRowLayout = null;
        List<ImageView> picImageViews = null;

        List<Post> posts = homepageData.getPosts();
        postsLayout.removeAllViewsInLayout();
        for (int i = 0; i < posts.size(); i++) {
            if (i % 3 == 0) {
                // new row
                postsRowLayout = LayoutInflater.from(this).inflate(R.layout.posts_row_item, postsLayout, false);
                postsLayout.addView(postsRowLayout);
                ImageView[] picImageViewsArray = {
                        (ImageView) postsRowLayout.findViewById(R.id.pic1_image_view),
                        (ImageView) postsRowLayout.findViewById(R.id.pic2_image_view),
                        (ImageView) postsRowLayout.findViewById(R.id.pic3_image_view)
                };
                picImageViews = Arrays.asList(picImageViewsArray);
                picImageViews.get(0).setMinimumHeight(picWidthOrHeight);
            }
            // load 3 pics per row (load the No.i%3 pic at a time)
            String picUrl = WeiwenApiClient.getPicUrl(posts.get(i).getUrl());
            mImageLoader.get(picUrl, ImageLoader.getImageListener(picImageViews.get(i%3),
                    R.color.cardview_light_background, R.color.cardview_light_background),
                    300, 300);
            // click pic to jump to DetailActivity
            final int curPostId = posts.get(i).getPostId();
            picImageViews.get(i%3).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(HomepageActivity.this, DetailActivity.class);
                    intent.putExtra("postId", curPostId);
                    startActivity(intent);
                }
            });
        }

        // setup follow button
        if (!isSelf) {
            if (homepageData.isFollowed()) {
                mFollowButton.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_unfo));
            } else {
                mFollowButton.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_add));
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return super.onOptionsItemSelected(item);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static int dp2px(Context context, float dpValue) {

        final float scale = context.getResources().getDisplayMetrics().density;

        return (int) (dpValue * scale +0.5f);

    }
}
