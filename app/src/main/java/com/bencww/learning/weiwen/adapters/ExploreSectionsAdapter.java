package com.bencww.learning.weiwen.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.bencww.learning.weiwen.HomepageActivity;
import com.bencww.learning.weiwen.R;
import com.bencww.learning.weiwen.models.ExploreSection;
import com.bencww.learning.weiwen.models.Comment;
import com.bencww.learning.weiwen.models.Post;
import com.bencww.learning.weiwen.utils.VolleyUtil;
import com.bencww.learning.weiwen.utils.WeiwenApiClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by BenWwChen on 2017/1/18.
 */

public class ExploreSectionsAdapter extends RecyclerView.Adapter<ExploreSectionsAdapter.ViewHolder> {

    private ExploreSectionsAdapterCallback mExploreSectionsAdapterCallback;
    private ArrayList<ExploreSection> mExploreSections;
    private ImageLoader mImageLoader;
    private Context context;
    private Handler mSetFollowHandler;
    private boolean mFollowAttempt;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView avatarImageView;
        public TextView userNameTextView;
        public TextView bioTextView;
        public Button followButton;
        public List<ImageView> picImageViews;

        public ViewHolder(View itemView) {
            super(itemView);
            avatarImageView = (ImageView) itemView.findViewById(R.id.avatar_image_view);
            userNameTextView = (TextView) itemView.findViewById(R.id.username_text_view);
            bioTextView = (TextView) itemView.findViewById(R.id.bio_text_view);
            followButton = (Button) itemView.findViewById(R.id.follow_button);
            ImageView[] picImageViewsArray = {
                    (ImageView) itemView.findViewById(R.id.pic1_image_view),
                    (ImageView) itemView.findViewById(R.id.pic2_image_view),
                    (ImageView) itemView.findViewById(R.id.pic3_image_view)
            };
            picImageViews = Arrays.asList(picImageViewsArray);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ExploreSectionsAdapter(ArrayList<ExploreSection> exploreSections, final Context context,
                                  ExploreSectionsAdapterCallback exploreSectionsAdapterCallback) {
        this.context = context;
        this.mExploreSections = exploreSections;
        this.mExploreSectionsAdapterCallback = exploreSectionsAdapterCallback;
        // Get the ImageLoader through your singleton class.
        mImageLoader = VolleyUtil.getInstance(context).getImageLoader();
        mSetFollowHandler = new Handler() {
            @Override
            public void handleMessage(android.os.Message msg) {
                boolean isSuccess;

                if(msg.what == 1){
                    isSuccess = (boolean) msg.obj;
                } else {
                    Log.d("error", (String) msg.obj);
                    Toast.makeText(context, "错误:" + msg.obj, Toast.LENGTH_SHORT).show();
                    isSuccess = false;
                }
                if (isSuccess) {
                    // updata data in the fragment
                    mExploreSectionsAdapterCallback.onSetFollow(mFollowAttempt);
                }
            }
        };
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ExploreSectionsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.explore_list_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        final ExploreSection curExploreSection = mExploreSections.get(position);

        // username, bio
        holder.userNameTextView.setText(curExploreSection.getUser().getUsername());
        holder.bioTextView.setText(curExploreSection.getUser().getBio());

        // avatar, (up to) 3 pics
        ImageView avatarImageView = holder.avatarImageView;
        List<ImageView> picImageViews = holder.picImageViews;

        String avatarUrl = WeiwenApiClient.getAvatarUrl(curExploreSection.getUser().getAvatar());
        mImageLoader.get(avatarUrl, ImageLoader.getImageListener(avatarImageView,
                R.color.cardview_light_background, R.color.cardview_light_background));

        // click username or avatar jump to homepage
        View.OnClickListener nameAvatarOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, HomepageActivity.class);
                intent.putExtra("username", curExploreSection.getUser().getUsername());
                context.startActivity(intent);
            }
        };
        holder.userNameTextView.setOnClickListener(nameAvatarOnClickListener);
        holder.avatarImageView.setOnClickListener(nameAvatarOnClickListener);

        // follow button
        holder.followButton.setText(curExploreSection.isFollowed()?
                R.string.following : R.string.follow);
        if (curExploreSection.isFollowed()) {
            holder.followButton.getBackground().setColorFilter(
                    ContextCompat.getColor(context, R.color.colorFollowing), PorterDuff.Mode.MULTIPLY);
        } else {
            holder.followButton.getBackground().setColorFilter(null);
        }

        holder.followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFollowAttempt = !curExploreSection.isFollowed();
                WeiwenApiClient.getInstance(context).setFollowByUsername(
                        curExploreSection.getUser().getUsername(),
                        !curExploreSection.isFollowed(), mSetFollowHandler);
            }
        });

        // clear the ImageViews
        for (int i = 0; i < 3; i++) {
            picImageViews.get(i).setImageResource(0);
        }

        List<Post> posts = curExploreSection.getPosts();
        if (posts != null) {
            for (int i = 0; i < posts.size(); i++) {
                String picUrl = WeiwenApiClient.getPicUrl(posts.get(i).getUrl());
                mImageLoader.get(picUrl, ImageLoader.getImageListener(picImageViews.get(i),
                        R.color.cardview_light_background, R.color.cardview_light_background),
                        300, 300);
            }
        }
    }

    public interface ExploreSectionsAdapterCallback {
        void onSetFollow(boolean follow); // tell the Explore Fragment to refresh data
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mExploreSections.size();
    }
}
