package com.bencww.learning.weiwen;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bencww.learning.weiwen.utils.WeiwenApiClient;

import java.io.ByteArrayOutputStream;

public class CreatePostActivity extends AppCompatActivity {

    private EditText mDescriptionEditText;
    private ImageView mThumbnailImageView;
    private View mCreatePostView;
    private View mProgressView;
    private UploadTask mUploadTask;

    private static final int RESULT_CODE_SUCCESS = 1;
    private static final int RESULT_CODE_FAILED = -1;

    private Bitmap mBitmap;
    private String mDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);
        setTitle("发布");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mThumbnailImageView = (ImageView) findViewById(R.id.thumbnail_image_view);
        mDescriptionEditText = (EditText) findViewById(R.id.description);
        mCreatePostView = findViewById(R.id.create_post_layout);
        mProgressView = findViewById(R.id.upload_progress);

        mBitmap = null;
        try {
            mBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),
                    this.getIntent().getData());
        } catch (Exception e) {
            Log.d("fail", "fail to create bitmap");
        }
        mThumbnailImageView.setImageBitmap(ThumbnailUtils.extractThumbnail(mBitmap, 1000, 1000));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_create_post, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.create_post) {
            Toast.makeText(this, "发布中", Toast.LENGTH_SHORT).show();
            attemptUpload();
            return true;
        } else if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 将Bitmap转换成Base64字符串
     * @param bit
     * @return
     */
    public String Bitmap2StrByBase64(Bitmap bit){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bit.compress(Bitmap.CompressFormat.JPEG, 100, bos);//参数100表示不压缩
        byte[] bytes=bos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }


    private void attemptUpload() {
        mDescription = mDescriptionEditText.getText().toString();
        showProgress(true);
        mUploadTask = new UploadTask(CreatePostActivity.this);
        mUploadTask.execute((Void) null);
    }

    public class UploadTask extends AsyncTask<Void, Void, Boolean> {

        Context context;

        UploadTask(Context context) {
            this.context = context.getApplicationContext();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean isSuccess;


            try {
                // check if cookie still valid
                isSuccess = WeiwenApiClient.getInstance(getApplicationContext())
                        .createPost(Bitmap2StrByBase64(mBitmap), mDescription);
            } catch (Exception e) {
                return false;
            }

            return isSuccess;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mUploadTask = null;

            if (success) {
                setResult(RESULT_CODE_SUCCESS);
                finish();
            } else {
                showProgress(false);
            }
        }

        @Override
        protected void onCancelled() {
            mUploadTask = null;
            showProgress(false);
        }
    }

    /**
     * Shows the progress UI and hides the create post form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mCreatePostView.setVisibility(show ? View.GONE : View.VISIBLE);
            mCreatePostView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mCreatePostView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mCreatePostView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
