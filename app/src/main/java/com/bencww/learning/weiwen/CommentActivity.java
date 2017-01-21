package com.bencww.learning.weiwen;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.bencww.learning.weiwen.adapters.CommentsAdapter;
import com.bencww.learning.weiwen.models.Comment;
import com.bencww.learning.weiwen.models.Post;
import com.bencww.learning.weiwen.utils.WeiwenApiClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommentActivity extends AppCompatActivity {

    // views
    private ListView mCommentsListView;
    private EditText mCommentEditText;
    private Button mCommentSubmitButton;

    private CommentsAdapter mCommentsAdapter;
    private ArrayList<Comment> mComments;
    private Handler mCommentsHandler;
    private Handler mSubmitHandler;
    private int mPostId;
    private static final int ERROR_CODE = -1;
    private static final int UPDATE_COMMENT = 1;
    private static final int NO_UPDATE_COMMENT = 0;
    private int result = NO_UPDATE_COMMENT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_comment);

        setTitle(R.string.title_comment);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mCommentsListView = (ListView) findViewById(R.id.comments_list_view);
        mComments = new ArrayList<>();
        mCommentsAdapter = new CommentsAdapter(this, mComments);
        mCommentsListView.setAdapter(mCommentsAdapter);

        // get/update comments handler
        mCommentsHandler = new Handler() {

            @Override
            public void handleMessage(android.os.Message msg) {
                List<Comment> comments;
                Post post;

                if(msg.what == 1){
                    post = (Post) msg.obj;
                    comments = post.getComments();
                } else {
                    Log.d("error", (String) msg.obj);
                    comments = Collections.EMPTY_LIST;
                }
                // specify an adapter
                mComments.clear();
                mComments.addAll(comments);
                mCommentsAdapter.notifyDataSetChanged();
                // scroll to the bottom
                mCommentsListView.post(new Runnable(){
                    public void run() {
                        mCommentsListView.setSelection(mCommentsListView.getCount() - 1);
                    }});
            };
        };

        mPostId = this.getIntent().getIntExtra("postId", ERROR_CODE);
        if (mPostId != ERROR_CODE) {
            WeiwenApiClient.getInstance(this).getPost(mPostId, mCommentsHandler);
        } else {
            // no post id passed, finish
            setResult(result);
            finish();
        }

        // setup edit text and submit button
        mCommentEditText = (EditText) findViewById(R.id.comment_edit_text);
        mCommentSubmitButton = (Button) findViewById(R.id.comment_submit_button);

        mSubmitHandler = new Handler() {

            @Override
            public void handleMessage(android.os.Message msg) {
                boolean isSuccess;

                if(msg.what == 1){
                    isSuccess = (boolean) msg.obj;
                    result = UPDATE_COMMENT; // tell Timeline to refresh data
                    setResult(result);
                } else {
                    Log.d("error", (String) msg.obj);
                    Toast.makeText(getApplicationContext(), "错误:" + msg.obj, Toast.LENGTH_SHORT).show();
                    isSuccess = false;
                }

                // refresh comment data
                WeiwenApiClient.getInstance(getApplicationContext()).getPost(mPostId, mCommentsHandler);
            };
        };

        mCommentSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submit();
            }
        });

        mCommentEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction()!=KeyEvent.ACTION_DOWN)
                    return true;

                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    finish();
                }
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    submit();
                    return true;
                }
                return false;
            }
        });

    }

    private void submit() {
        String commentContent = mCommentEditText.getText().toString();
        if (commentContent.isEmpty()) {
            mCommentEditText.setError("你还没输入内容");
        } else {
            WeiwenApiClient.getInstance(getApplicationContext())
                    .commentOnAPost(mPostId, commentContent, mSubmitHandler);
            mCommentEditText.setText("");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

}
