package com.bencww.learning.weiwen.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bencww.learning.weiwen.HomepageActivity;
import com.bencww.learning.weiwen.R;
import com.bencww.learning.weiwen.models.Comment;

import java.util.ArrayList;

/**
 * Created by BenWwChen on 2017/1/14.
 */

public class CommentsAdapter extends ArrayAdapter<Comment> {

    Context context;

    public CommentsAdapter(Context context, ArrayList<Comment> comments) {
        super(context, 0, comments);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final Comment comment = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.comment_list_item, parent, false);
        }
        // Lookup view for data population
        TextView userNameTextView = (TextView) convertView.findViewById(R.id.comment_username_text_view);
        TextView contentTextView = (TextView) convertView.findViewById(R.id.comment_content_text_view);
        // Populate the data into the template view using the data object
        userNameTextView.setText(comment.getUsername());
        contentTextView.setText(comment.getContent());
        userNameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, HomepageActivity.class);
                intent.putExtra("username", comment.getUsername());
                context.startActivity(intent);
            }
        });
        // Return the completed view to render on screen
        return convertView;
    }
}
