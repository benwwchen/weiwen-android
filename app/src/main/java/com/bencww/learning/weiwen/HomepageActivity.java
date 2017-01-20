package com.bencww.learning.weiwen;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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

    HomepageData homepageData;
    ImageLoader mImageLoader;
    int picWidthOrHeight; // width/height of every pic

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mImageLoader = VolleyUtil.getInstance(this).getImageLoader();

        setContentView(R.layout.activity_homepage);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // calculate the width/height of the pics on screen
        DisplayMetrics dm = getResources().getDisplayMetrics();
        picWidthOrHeight = (dm.widthPixels - dp2px(this, 64)) / 3;

        // get user data
        String username = this.getIntent().getStringExtra("username");

        Handler dataHandler = new Handler() {

            @Override
            public void handleMessage(android.os.Message msg) {

                if(msg.what == 1){
                    homepageData = (HomepageData) msg.obj;
                } else {
                    Log.d("error", (String) msg.obj);
                }

                // load data into views
                if (homepageData != null) {
                    loadData();
                }
            }
        };

        WeiwenApiClient.getInstance(this).getHomepageData(username, dataHandler);

    }

    private void loadData() {
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
