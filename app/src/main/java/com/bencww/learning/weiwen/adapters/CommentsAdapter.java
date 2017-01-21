package com.bencww.learning.weiwen.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.bencww.learning.weiwen.HomepageActivity;
import com.bencww.learning.weiwen.R;
import com.bencww.learning.weiwen.models.Comment;
import com.bencww.learning.weiwen.utils.VolleyUtil;
import com.bencww.learning.weiwen.utils.WeiwenApiClient;

import java.util.ArrayList;

/**
 * Created by BenWwChen on 2017/1/14.
 */

public class CommentsAdapter extends ArrayAdapter<Comment> {

    Context context;
    ImageLoader mImageLoader;

    public CommentsAdapter(Context context, ArrayList<Comment> comments) {
        super(context, 0, comments);
        this.context = context;
        mImageLoader = VolleyUtil.getInstance(context).getImageLoader();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final Comment comment = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.comment_detail_list_item, parent, false);
        }

        // avatar, username
        ImageView avatarImageView = (ImageView) convertView.findViewById(R.id.avatar_image_view);
        TextView userNameTextView = (TextView) convertView.findViewById(R.id.comment_username_text_view);

        String avatarUrl = WeiwenApiClient.getAvatarUrl(comment.getAvatar());
        mImageLoader.get(avatarUrl, ImageLoader.getImageListener(avatarImageView,
                R.color.cardview_light_background, R.color.cardview_light_background), 100, 100);

        userNameTextView.setText(comment.getUsername());

        View.OnClickListener nameAvatarOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, HomepageActivity.class);
                intent.putExtra("username", comment.getUsername());
                context.startActivity(intent);
            }
        };
        avatarImageView.setOnClickListener(nameAvatarOnClickListener);
        userNameTextView.setOnClickListener(nameAvatarOnClickListener);

        // content, create time
        TextView contentTextView = (TextView) convertView.findViewById(R.id.comment_content_text_view);
        TextView timeTextView = (TextView) convertView.findViewById(R.id.time_text_view);

        contentTextView.setText(comment.getContent());
        timeTextView.setText(comment.getCreateTime());

        // Return the completed view to render on screen
        return convertView;
    }
}
