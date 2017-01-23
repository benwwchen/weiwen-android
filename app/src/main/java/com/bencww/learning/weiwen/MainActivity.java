package com.bencww.learning.weiwen;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcel;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ActionProvider;
import android.view.ContextMenu;
import android.view.SubMenu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.bencww.learning.weiwen.fragments.ExploreFragment;
import com.bencww.learning.weiwen.fragments.TimelineFragment;
import com.bencww.learning.weiwen.models.User;
import com.bencww.learning.weiwen.utils.VolleyUtil;
import com.bencww.learning.weiwen.utils.WeiwenApiClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        TimelineFragment.OnFragmentInteractionListener,
        ExploreFragment.OnFragmentInteractionListener {

    private TimelineFragment timelineFragment;
    private ExploreFragment exploreFragment;

    FloatingActionButton mFab;

    ImageLoader mImageLoader;

    User userMe;

    private static final int REQUEST_CODE_TAKE_PHOTO = 0;// 拍照请求码
    private static final int REQUEST_CODE_CHOOSE_FROM_ALBUM = 1;// 相册选择请求码
    private static final int REQUEST_CODE_CLIP_PHOTO = 2;// 裁剪请求码
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 3; // 存储权限
    private static final int REQUEST_CODE_CREATE_POST = 4; // 发布图片
    private static final int RESULT_CODE_SUCCESS = 1;
    private static final int RESULT_CODE_FAILED = -1;

    private File mOutputFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
                // let the user choose to take photo or choose from album
                handleFAB();
            }
        });

        // initiate view
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // initialize navigation header
        View headerLayout = navigationView.getHeaderView(0);
        TextView usernameTextView = (TextView) headerLayout.findViewById(R.id.username_text_view);
        TextView userBioTextView = (TextView) headerLayout.findViewById(R.id.user_bio_text_view);
        ImageView userAvatarImageView = (ImageView) headerLayout.findViewById(R.id.user_avatar_image_view);
        headerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMyHomepageActivity();
            }
        });

        userMe = WeiwenApiClient.getInstance(getApplicationContext()).getUser();

        usernameTextView.setText(userMe.getUsername());
        userBioTextView.setText(userMe.getBio());

        mImageLoader = VolleyUtil.getInstance(getApplicationContext()).getImageLoader();
        mImageLoader.get(userMe.getAvatar(), ImageLoader.getImageListener(userAvatarImageView,
                R.color.cardview_light_background, R.color.cardview_light_background));

        if (savedInstanceState == null) {
            // load fragment only at the first time the activity created
            try {
                timelineFragment = TimelineFragment.newInstance();
                exploreFragment = ExploreFragment.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.flContent, timelineFragment).commit();
            getSupportActionBar().setTitle(R.string.navigation_drawer_timeline);
        }

    }

    private void handleFAB() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("发布图片");
        builder.setMessage("选择照片来源");
        builder.setNegativeButton("从相册中选择", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                chooseFromAlbum();
            }
        });
        builder.setPositiveButton("拍照", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                takePhoto();
            }
        });
        builder.show();
    }

    private void chooseFromAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, REQUEST_CODE_CHOOSE_FROM_ALBUM);
    }

    private void takePhoto() {
            /*+++++++针对6.0及其以上系统，读写外置存储权限的检测+++++++++*/
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            String [] permissions = {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                    , Manifest.permission.READ_EXTERNAL_STORAGE
            };
            ActivityCompat.requestPermissions(MainActivity.this, permissions, REQUEST_CODE_STORAGE_PERMISSION);
            return;
        }
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String sdPath = Environment.getExternalStorageDirectory()
                    .getAbsolutePath();
            mOutputFile = new File(sdPath, System.currentTimeMillis() + ".jpg");//拍照之后照片的路径
            try {
                if (!mOutputFile.exists()) {
                    mOutputFile.createNewFile();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Uri uri= FileProvider.getUriForFile(this,
                    "com.bencww.learning.takePhoto.provider", mOutputFile);
            Log.i("take", "takePhoto: uri:===" + uri);
            Intent newIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//设置Action为拍照
            newIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);//将拍取的照片保存到指定Uri
            startActivityForResult(newIntent, REQUEST_CODE_TAKE_PHOTO);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "并未获取到存储权限", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        // permissons granted, try again
        takePhoto();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_TAKE_PHOTO) {
            onPhotoTaken(resultCode, data);
        } else if (requestCode == REQUEST_CODE_CHOOSE_FROM_ALBUM) {
            onPhotoChosen(resultCode, data);
        } else if (requestCode == REQUEST_CODE_CLIP_PHOTO) {
            onPhotoCliped(resultCode, data);
        } else if (requestCode == REQUEST_CODE_CREATE_POST) {
            if (resultCode == RESULT_CODE_SUCCESS) {
                Snackbar.make(mFab, "发布成功", Snackbar.LENGTH_LONG).show();
                if (timelineFragment != null) {
                    timelineFragment.refreshData();
                }
            }
        }
    }

    /**
     * 拍照完成
     *
     * @param resultCode
     * @param data
     */
    private void onPhotoTaken(int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "已取消", Toast.LENGTH_SHORT)
                    .show();
            return;
        } else if (resultCode != RESULT_OK) {
            Toast.makeText(this, "拍照失败", Toast.LENGTH_SHORT)
                    .show();
        } else {
            /*调用裁剪图片的方法进行裁剪图片*/
            Uri uri = FileProvider.getUriForFile(this,
                    "com.bencww.learning.takePhoto.provider", mOutputFile);
            clipPhoto(uri);
        }
    }

    /**
     * 选择照片完成
     *
     * @param resultCode
     * @param data
     */
    private void onPhotoChosen(int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "已取消", Toast.LENGTH_SHORT)
                    .show();
            return;
        } else if (resultCode != RESULT_OK) {
            Toast.makeText(this, "拍照失败", Toast.LENGTH_SHORT)
                    .show();
        } else {
            // 写入新文件
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                String [] permissions = {
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                        , Manifest.permission.READ_EXTERNAL_STORAGE
                };
                ActivityCompat.requestPermissions(MainActivity.this, permissions, REQUEST_CODE_STORAGE_PERMISSION);
                return;
            }
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                String sdPath = Environment.getExternalStorageDirectory()
                        .getAbsolutePath();
                mOutputFile = new File(sdPath, System.currentTimeMillis() + ".jpg");//拍照之后照片的路径
                try {
                    if (!mOutputFile.exists()) {
                        mOutputFile.createNewFile();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // write the source to our new file
                final int chunkSize = 1024;  // We'll read in one kB at a time
                byte[] imageData = new byte[chunkSize];
                InputStream inputStream;
                OutputStream outputStream;
                try {
                    inputStream = getContentResolver().openInputStream(data.getData());
                    outputStream = new FileOutputStream(mOutputFile);

                    int bytesRead;
                    while ((bytesRead = inputStream.read(imageData)) > 0) {
                        outputStream.write(Arrays.copyOfRange(imageData, 0, Math.max(0, bytesRead)));
                    }
                    inputStream.close();
                    outputStream.close();
                } catch (Exception ex) {
                    Log.e("Something went wrong.", ex.getMessage());
                }
                /*调用裁剪图片的方法进行裁剪图片*/
                clipPhoto(FileProvider.getUriForFile(this,
                        "com.bencww.learning.takePhoto.provider", mOutputFile));
            }
        }
    }
    /**
     * 裁剪照片
     *
     * @param uri
     *
     */
    private void clipPhoto(Uri uri) {
        String[] imageSource = new String[] {
                "com.android.camera"
                //"com.android.externalstorage","com.android.providers.downloads","com.android.providers.media"
        };
        for (String packageName : imageSource) {
            this.grantUriPermission(packageName, uri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//请求URI授权读取
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);//请求URI授权写入
        intent.setDataAndType(uri, "image/*");
        // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, REQUEST_CODE_CLIP_PHOTO);
    }
    /**
     * 裁剪照片完成
     *
     * @param resultCode
     * @param data
     */
    private void onPhotoCliped(int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "已取消", Toast.LENGTH_SHORT)
                    .show();
            return;
        } else if (resultCode != RESULT_OK) {
            Toast.makeText(this, "裁剪失败", Toast.LENGTH_SHORT)
                    .show();
        }
        // start CreatePostActivity here
        Intent intent = new Intent(MainActivity.this, CreatePostActivity.class);
        Uri uri= FileProvider.getUriForFile(this,
                "com.bencww.learning.takePhoto.provider", mOutputFile);
        intent.setData(uri);
        startActivityForResult(intent, REQUEST_CODE_CREATE_POST);
    }

    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            if(timelineFragment.isVisible()) {
                timelineFragment.refresh();
            } else if (exploreFragment.isVisible()) {
                exploreFragment.refresh();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_timeline) {
            getSupportActionBar().setTitle(R.string.navigation_drawer_timeline);
            displayTimelineFragment();
        } else if (id == R.id.nav_explore) {
            getSupportActionBar().setTitle(R.string.navigation_drawer_explore);
            displayExploreFragment();
        } else if (id == R.id.nav_me) {
            startMyHomepageActivity();
            item.setCheckable(false);
        } else if (id == R.id.nav_logout) {
            WeiwenApiClient.getInstance(this).logout();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_about) {
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    protected void displayTimelineFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (timelineFragment.isAdded()) { // if the fragment is already in container
            fragmentTransaction.show(timelineFragment);
        } else { // fragment needs to be added to frame container
            fragmentTransaction.add(R.id.flContent, timelineFragment, "timeline");
        }
        // Hide explore fragment
        if (exploreFragment.isAdded()) { fragmentTransaction.hide(exploreFragment); }
        // Commit changes
        fragmentTransaction.commit();
    }

    protected void displayExploreFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (exploreFragment.isAdded()) { // if the fragment is already in container
            fragmentTransaction.show(exploreFragment);
        } else { // fragment needs to be added to frame container
            fragmentTransaction.add(R.id.flContent, exploreFragment, "explore");
        }
        // Hide timeline fragment
        if (timelineFragment.isAdded()) { fragmentTransaction.hide(timelineFragment); }
        // Commit changes
        fragmentTransaction.commit();
    }

    protected void startMyHomepageActivity() {
        Intent intent = new Intent(MainActivity.this, HomepageActivity.class);
        intent.putExtra("username", userMe.getUsername());
        startActivity(intent);
    }

    @Override
    public void onFragmentInteraction(boolean isEmpty) {
        if (isEmpty) {
            // empty timeline switch to explore fragment
            Toast.makeText(getApplicationContext(),
                    getString(R.string.notification_explore),Toast.LENGTH_LONG).show();
            getSupportActionBar().setTitle(R.string.navigation_drawer_explore);
            displayExploreFragment();
        }
    }
}
