package com.bencww.learning.weiwen;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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

import com.android.volley.toolbox.ImageLoader;
import com.bencww.learning.weiwen.fragments.ExploreFragment;
import com.bencww.learning.weiwen.fragments.TimelineFragment;
import com.bencww.learning.weiwen.models.User;
import com.bencww.learning.weiwen.utils.VolleyUtil;
import com.bencww.learning.weiwen.utils.WeiwenApiClient;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        TimelineFragment.OnFragmentInteractionListener,
        ExploreFragment.OnFragmentInteractionListener {

    private TimelineFragment timelineFragment;
    private ExploreFragment exploreFragment;

    ImageLoader mImageLoader;

    User userMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // start CreatePostActivity here
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
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
}
